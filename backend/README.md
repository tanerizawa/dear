# Backend

This directory hosts the FastAPI service for Dear Diary.

## Setup

Create and activate a virtual environment, then install dependencies:

```bash
python -m venv venv
source venv/bin/activate
pip install -r requirements.txt
```

Run the development server from within this directory:

```bash
uvicorn app.main:app --reload
```
