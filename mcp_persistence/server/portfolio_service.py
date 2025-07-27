"""
Portfolio Service Layer
Handles business logic, parameter transformation, and acts as an adapter 
between the MCP controller layer and the persistence layer.
"""

import inspect
import asyncio
from typing import Dict, Any, Callable, Type, get_type_hints, get_origin
from pydantic import BaseModel
import logging

logger = logging.getLogger(__name__)

class PortfolioService:
    """
    Portfolio Service Layer
    
    Responsibilities:
    - Maps HTTP request parameters to tool function parameters
    - Validates input data using Pydantic models
    - Executes portfolio business logic
    - Acts as adapter between MCP controller and persistence layer
    - Generates API schemas for tools
    """
    
    def __init__(self):
        self.tool_cache = {}
    
    def analyze_tool(self, tool_func: Callable) -> Dict[str, Any]:
        """
        Analyze a tool function to determine its parameter structure.
        
        Returns:
            Dict containing parameter information and transformation strategy
        """
        func_name = tool_func.__name__
        
        # Cache analysis results
        if func_name in self.tool_cache:
            return self.tool_cache[func_name]
        
        try:
            # Get function signature
            signature = inspect.signature(tool_func)
            parameters = signature.parameters
            type_hints = get_type_hints(tool_func)
            
            analysis = {
                "name": func_name,
                "parameters": {},
                "strategy": "individual",  # or "structured"
                "model_class": None,
                "is_async": asyncio.iscoroutinefunction(tool_func)
            }
            
            # Analyze each parameter
            for param_name, param in parameters.items():
                param_info = {
                    "name": param_name,
                    "required": param.default == param.empty,
                    "default": param.default if param.default != param.empty else None,
                    "annotation": type_hints.get(param_name)
                }
                
                # Check if parameter is a Pydantic model
                if param_info["annotation"] and self._is_pydantic_model(param_info["annotation"]):
                    analysis["strategy"] = "structured"
                    analysis["model_class"] = param_info["annotation"]
                    analysis["model_param_name"] = param_name
                    
                    # Get the model's field information (handle both Pydantic v1 and v2)
                    model_class = param_info["annotation"]
                    analysis["model_fields"] = {}
                    
                    if hasattr(model_class, 'model_fields'):
                        # Pydantic v2
                        model_fields = model_class.model_fields
                        for field_name, field in model_fields.items():
                            analysis["model_fields"][field_name] = {
                                "required": field.is_required(),
                                "default": field.default,
                                "type": field.annotation if hasattr(field, 'annotation') else str,
                                "description": field.description or ""
                            }
                    elif hasattr(model_class, '__fields__'):
                        # Pydantic v1
                        model_fields = model_class.__fields__
                        for field_name, field in model_fields.items():
                            analysis["model_fields"][field_name] = {
                                "required": field.is_required(),
                                "default": field.default,
                                "type": field.type_,
                                "description": getattr(field.field_info, 'description', '') or ""
                            }
                    else:
                        # Fallback: try to get fields from model instance
                        try:
                            instance = model_class()
                            for field_name in instance.__dict__.keys():
                                analysis["model_fields"][field_name] = {
                                    "required": True,
                                    "default": None,
                                    "type": str,
                                    "description": f"Field {field_name}"
                                }
                        except:
                            pass
                
                analysis["parameters"][param_name] = param_info
            
            self.tool_cache[func_name] = analysis
            logger.debug(f"Analyzed tool {func_name}: strategy={analysis['strategy']}")
            return analysis
            
        except Exception as e:
            logger.error(f"Failed to analyze tool {func_name}: {e}")
            # Fallback to individual parameter strategy
            return {
                "name": func_name,
                "strategy": "individual",
                "parameters": {},
                "is_async": asyncio.iscoroutinefunction(tool_func)
            }
    
    def _is_pydantic_model(self, type_annotation: Type) -> bool:
        """Check if a type annotation is a Pydantic model"""
        try:
            return (
                inspect.isclass(type_annotation) and 
                issubclass(type_annotation, BaseModel)
            )
        except (TypeError, AttributeError):
            return False
    
    def map_parameters(self, tool_func: Callable, arguments: Dict[str, Any]) -> tuple:
        """
        Map HTTP request arguments to tool function parameters.
        
        Returns:
            Tuple of (args, kwargs) ready for function call
        """
        analysis = self.analyze_tool(tool_func)
        
        if analysis["strategy"] == "structured":
            return self._map_structured_parameters(analysis, arguments)
        else:
            return self._map_individual_parameters(analysis, arguments)
    
    def _map_structured_parameters(self, analysis: Dict[str, Any], arguments: Dict[str, Any]) -> tuple:
        """Map arguments to a structured Pydantic model"""
        try:
            model_class = analysis["model_class"]
            model_param_name = analysis["model_param_name"]
            
            # Handle special cases for tools with additional parameters
            if model_param_name == "input_data":
                # Simple case: just create the model from arguments
                model_instance = model_class(**arguments)
                return (), {model_param_name: model_instance}
            
            elif len(analysis["parameters"]) > 1:
                # Complex case: tool has multiple parameters including a model
                # We need to separate model fields from other parameters
                model_fields = set(analysis["model_fields"].keys())
                
                # Split arguments into model args and other args
                model_args = {k: v for k, v in arguments.items() if k in model_fields}
                other_args = {k: v for k, v in arguments.items() if k not in model_fields}
                
                # Create model instance
                model_instance = model_class(**model_args)
                other_args[model_param_name] = model_instance
                
                return (), other_args
            
            else:
                # Single parameter that is a model
                model_instance = model_class(**arguments)
                return (model_instance,), {}
                
        except Exception as e:
            logger.error(f"Failed to map structured parameters: {e}")
            raise ValueError(f"Parameter mapping failed: {e}")
    
    def _map_individual_parameters(self, analysis: Dict[str, Any], arguments: Dict[str, Any]) -> tuple:
        """Map arguments to individual function parameters"""
        try:
            # Validate required parameters
            for param_name, param_info in analysis["parameters"].items():
                if param_info["required"] and param_name not in arguments:
                    raise ValueError(f"Missing required parameter: {param_name}")
            
            # Filter arguments to only include valid parameters
            valid_params = set(analysis["parameters"].keys())
            filtered_args = {k: v for k, v in arguments.items() if k in valid_params}
            
            return (), filtered_args
            
        except Exception as e:
            logger.error(f"Failed to map individual parameters: {e}")
            raise ValueError(f"Parameter mapping failed: {e}")
    
    async def execute_tool(self, tool_func: Callable, arguments: Dict[str, Any]) -> Any:
        """
        Execute a tool with automatic parameter mapping.
        
        Args:
            tool_func: The tool function to execute
            arguments: Raw arguments from HTTP request
            
        Returns:
            Tool execution result
        """
        try:
            # Map parameters
            args, kwargs = self.map_parameters(tool_func, arguments)
            
            # Execute tool
            if asyncio.iscoroutinefunction(tool_func):
                result = await tool_func(*args, **kwargs)
            else:
                result = tool_func(*args, **kwargs)
            
            logger.debug(f"Successfully executed tool {tool_func.__name__}")
            return result
            
        except Exception as e:
            logger.error(f"Tool execution failed for {tool_func.__name__}: {e}")
            raise ValueError(f"Tool execution failed: {e}")
    
    def get_tool_schema(self, tool_func: Callable) -> Dict[str, Any]:
        """
        Generate OpenAPI-like schema for a tool based on its parameter structure.
        
        Returns:
            Schema dictionary that can be used for API documentation
        """
        analysis = self.analyze_tool(tool_func)
        
        schema = {
            "name": analysis["name"],
            "description": tool_func.__doc__ or f"Execute {analysis['name']}",
            "type": "object",
            "properties": {},
            "required": []
        }
        
        if analysis["strategy"] == "structured" and analysis.get("model_fields"):
            # Use model fields for schema
            for field_name, field_info in analysis["model_fields"].items():
                schema["properties"][field_name] = {
                    "type": self._python_type_to_json_type(field_info["type"]),
                    "description": field_info.get("description", "")
                }
                if field_info["required"]:
                    schema["required"].append(field_name)
        else:
            # Use function parameters for schema
            for param_name, param_info in analysis["parameters"].items():
                if param_name not in ["self", "cls"]:  # Skip special parameters
                    schema["properties"][param_name] = {
                        "type": self._python_type_to_json_type(param_info["annotation"]),
                        "description": f"Parameter {param_name}"
                    }
                    if param_info["required"]:
                        schema["required"].append(param_name)
        
        return schema
    
    def _python_type_to_json_type(self, python_type) -> str:
        """Convert Python type hints to JSON schema types"""
        if python_type is None:
            return "string"
        
        type_mapping = {
            str: "string",
            int: "integer", 
            float: "number",
            bool: "boolean",
            list: "array",
            dict: "object"
        }
        
        # Handle typing module types
        origin = get_origin(python_type)
        if origin is not None:
            return type_mapping.get(origin, "string")
        
        return type_mapping.get(python_type, "string")

# Global service instance
portfolio_service = PortfolioService() 