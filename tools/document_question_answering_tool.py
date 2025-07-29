from pydantic import BaseModel, Field
from langchain.tools import BaseTool
from PIL import Image
from transformers import pipeline
import requests

from typing import Type, Final, Any

DOCUMENT_QUESTION_MODEL: Final[str] = 'impira/layoutlm-document-qa'

class DocumentQuestionAnsweringInput(BaseModel):
    query: str = Field(description='Your question about the document')
    document_url: str = Field(description='URL to the document.')

class DocumentQuestionAnsweringTool(BaseTool):

    name: str = "document_question_answering_tool"
    description: str = """
        Use this tool when asked to answer questions about documents.
        This tool is designed to answer basic factual questions based on the contents of a text-based document accessible via a public URL. It supports simple information retrieval tasks such as:
            - Identifying key facts (e.g., “What is the date of the agreement?”)
            - Locating named entities (e.g., “Who signed the contract?”)
            - Extracting short text spans or direct answers from the document (e.g., “What is the total cost?”)
    """
    args_schema: Type[BaseModel] = DocumentQuestionAnsweringInput

    def __init__(self, **kwargs: Any) -> None:
        super().__init__()

    def _run(self, document_url: str, query: str) -> str:
        """
            Perform basic visual question answering on documents accessible via a public URL.
            Args:
                document_url (str): The URL to the document to analyze.
                query (str): Your question about the provided document.

            Returns:
                str: An answer for the query based on the document content
        """
        pipe = pipeline("document-question-answering", model="impira/layoutlm-document-qa")

        # Load document from URL
        document = Image.open(requests.get(document_url, stream=True).raw)

        result = pipe(image=document, question=query)[0]

        return f"Answer: '{result['answer']}' - (Precission: {result['score']})"
