import os

from pydantic import BaseModel, Field
from google import genai
from google.genai import types
from langchain.schema import SystemMessage
from langchain.tools import BaseTool

from typing import Type, Final, Any

GEMINI_MODEL: Final[str] = 'gemini-2.5-flash-preview-05-20'

class VideoAnalyzerInput(BaseModel):
    video_url: str = Field(description='URL to the video to analyze and apply the query on.')
    query: str = Field(description="The query about the video.")

class VideoAnalyzer(BaseTool):

    name: str = "video_analyzer"
    description: str = (
        "Given a video URL and a query, it return a response for the query on that video."
    )
    args_schema: Type[BaseModel] = VideoAnalyzerInput

    def __init__(self, **kwargs: Any) -> None:
        super().__init__()
        self.__client = genai.Client(api_key=os.environ["GOOGLE_API_KEY"])

    def _run(self, video_url: str, query: str) -> str:
        """
            Given a video URL and a query, it return a response for the query on that video
            Args:
                video_url (str): The URL to the image to analyze
                query (str): The query on the video

            Returns:
                str: An answer for the query based on the video content
        """
        response = self.__client.models.generate_content(model=GEMINI_MODEL, contents=types.Content(parts=[types.Part(file_data=types.FileData(file_uri=video_url)), types.Part(text=query)]))

        return response.text
