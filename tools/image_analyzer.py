from pydantic import BaseModel, Field
from langchain.schema import SystemMessage
from langchain.tools import BaseTool
from langchain_core.messages import SystemMessage, HumanMessage
from langchain_openai import ChatOpenAI

from typing import Type, Final, Any
    

DEFAULT_MULTIMODAL_LLM: Final[str] = 'gpt-4o-mini'

MULTIMODAL_LLM: Final[str] = 'multimodal_llm'

class ImageAnalyzerInput(BaseModel):
    image_url: str = Field(description='URL to the image to analyze.')

class ImageAnalyzer(BaseTool):

    name: str = "image_analyzer"
    description: str = (
        "Given an image URL it returns a general description of what appears in the image."
    )
    args_schema: Type[BaseModel] = ImageAnalyzerInput

    def __init__(self, **kwargs: Any) -> None:
        super().__init__()

        if MULTIMODAL_LLM in kwargs:
            self._vllm = kwargs[MULTIMODAL_LLM]
        else:
            self._vllm = ChatOpenAI(model=DEFAULT_MULTIMODAL_LLM)

    def _run(self, image_url: str) -> str:
        """
            Given the URL to an image, it return a general description of what appears in it.
            Args:
                image_url (str): The URL to the image to analyze

            Returns:
                str: An answer for the query based on the image content
        """
        system_prompt = SystemMessage(content="""
            You are a highly capable multimodal language model specialized in visual analysis. Your task is to interpret and analyze the provided image. Carefully consider the visual content to deliver a clear, accurate, and relevant response.
            Always prioritize factual and useful insights based on the image, and avoid making assumptions beyond what is visible.
        """)
        query = 'Describe what you see in the provided image'
        user_message = HumanMessage(
            content=[
                {"type": "text", "text": query},
                {
                    "type": "image_url",
                    "image_url": {"url": image_url}
                },
            ],
        )

        response = self._vllm.invoke([system_prompt, user_message])

        return response
