# Backend

This directory hosts the FastAPI service for Dear Diary.

## Setup

Create and activate a virtual environment, then install dependencies:

```bash
python -m venv venv
source venv/bin/activate
pip install -r requirements.txt
```

> **Note**: Running `pip install -r requirements.txt` is mandatory before
> starting Uvicorn. Omitting it can lead to import errors such as
> `ModuleNotFoundError` if required packages are missing.

Run the development server from within this directory:

```bash
uvicorn app.main:app --reload
```
