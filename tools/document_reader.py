from pydantic import BaseModel, Field
from langchain.tools import BaseTool

from typing import Type, Any

import pandas as pd
import requests
import io
import PyPDF2

class DocumentReaderInput(BaseModel):
    file_url: str = Field(description='The URL to the file.')
    file_format: str = Field(description="The file format (one of: xlsx, csv, txt, pdf).")

class DocumentReader(BaseTool):

    name: str = "document_reader_tool"
    description: str = (
        "Given the URL to a file and its format, return the file content."
        "Use this tool when you need to read the content of a file in a document format like PDF, xlsx, csv, txt..."
        "This is not an OCR tool, so scanned documents cannot be read using this tool."
    )
    args_schema: Type[BaseModel] = DocumentReaderInput

    def __init__(self, **kwargs: Any) -> None:
        super().__init__()

    def _run(self, file_url: str, file_format: str) -> str:
        """
            Given the URL to a file and its format, return the file content.
            Args:
                file_url (str): The URL to the file.
                file_format (str): The file format (one of: xlsx, csv, txt, pdf).

            Returns:
                str: The file content
        """
        file_format = file_format.lower()

        if file_format == 'xlsx':
            return str(read_excel_file(file_url=file_url))
        elif file_format == 'csv':
            return str(read_csv_file(file_url=file_url))
        elif file_format == 'txt':
            return read_txt_file(file_url=file_url)
        elif file_format == 'pdf':
            return read_pdf_file(file_url=file_url)
        else:
            raise ValueError(f"Unsupported file format: {file_format}")
    


def read_txt_file(file_url: str):
    try:
        response = requests.get(file_url)
        response.raise_for_status()  # Raises HTTPError for bad responses (4xx or 5xx)
    
        
        return response.text

    except requests.exceptions.RequestException as e:
        raise RuntimeError(f"Error fetching the file: {e}")
    
def read_excel_file(file_url: str):
    try:
        response = requests.get(file_url)
        response.raise_for_status()

        excel_data = pd.read_excel(io.BytesIO(response.content), sheet_name=None)
        return {sheet: df.head().to_string() for sheet, df in excel_data.items()}

    except requests.exceptions.RequestException as e:
        raise RuntimeError(f"Error fetching the Excel file: {e}")

def read_csv_file(file_url: str, max_rows: int = 100):
    try:
        response = requests.get(file_url)
        response.raise_for_status()

        df = pd.read_csv(io.StringIO(response.text))

        if max_rows:
            df = df.head(max_rows)

        return df.to_string()

    except requests.exceptions.RequestException as e:
        raise RuntimeError(f"Error fetching the CSV file: {e}")

def read_pdf_file(file_url: str):
    try:
        response = requests.get(file_url)
        response.raise_for_status()

        with io.BytesIO(response.content) as file_stream:
            reader = PyPDF2.PdfReader(file_stream)
            text = ''
            for page in reader.pages:
                text += page.extract_text() or ''

            return text.strip()

    except requests.exceptions.RequestException as e:
        raise RuntimeError(f"Error fetching the PDF file: {e}")

