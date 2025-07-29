from langchain_community.document_loaders import YoutubeLoader
from langchain_community.document_loaders.youtube import TranscriptFormat
from langchain.docstore.document import Document

from pydantic import BaseModel, Field
from langchain.tools import BaseTool

from typing import Type, Any, List
    

class YoutubeVideoTranscript(BaseModel):
    video_url: str = Field(description='URL to the video to get the transcription for.')
    language: str = Field(description="The transcription language.")

class YoutubeVideoTranscriptTool(BaseTool):

    name: str = "youtubue_video_transcript"
    description: str = (
        "Given an URL to a YouTube video, returns the transcription for that video."
    )
    args_schema: Type[BaseModel] = YoutubeVideoTranscript

    def __init__(self, **kwargs: Any) -> None:
        super().__init__()

    def _run(self, video_url: str, language: str) -> str:
        """
            Given an URL to a YouTube video, returns the transcription for that video.
            Args:
                video_url (str): URL to the video to get the transcription for.
                language (str): The transcription language.

            Returns:
                List[Document]: The video transcriptions in chunks of 30 seconds.
        """
        loader = YoutubeLoader.from_youtube_url(video_url, add_video_info = False, language = [language], transcript_format=TranscriptFormat.CHUNKS, translation=language, chunk_size_seconds=30)

        transcriptions: List[Document] = loader.load()

        return transcriptions
