# Dear Diary

## Overview

**Dear Diary** is a personal journaling application consisting of:

- **Android app**: Built with Kotlin, Jetpack Compose, and Hilt. The source lives under `app/`.
- **FastAPI backend**: Provides RESTful endpoints for authentication and journal management. The source is in `backend/app/`.

### Architecture

The Android module follows the MVVM pattern and communicates with the backend using Retrofit. Room is used for local persistence and Hilt provides dependency injection. The backend is a FastAPI project using SQLAlchemy ORM and JWT based authentication.

## Android Build

1. Ensure you have the Android SDK installed.
2. Build from the command line:

```bash
./gradlew assembleDebug
```

   or open the project in **Android Studio** and run the *app* configuration.

## Backend Setup

1. Create and activate a Python virtual environment:

```bash
python -m venv venv
source venv/bin/activate
```

2. Install dependencies:

```bash
pip install -r backend/requirements.txt
```

3. Start the development server **from inside** the `backend/` directory to avoid
   `ModuleNotFoundError`:

```bash
cd backend && uvicorn app.main:app --reload
```

### Environment Variables

The backend reads several variables from the environment:

- `DATABASE_URL` – SQLAlchemy database URL (defaults to SQLite `sqlite:///./test.db`).
- `SECRET_KEY` – secret key used for JWT creation (defaults to `supersecretkey`).
- `OPENROUTER_API_KEY` – API key for the OpenRouter chat service.
- `PLANNER_MODEL_NAME` – model name used by the conversation planner.
- `GENERATOR_MODEL_NAME` – model name used by the response generator.
- `APP_SITE_URL` – site URL sent in OpenRouter requests for identification.
- `APP_NAME` – application name reported to OpenRouter when making requests.

## Usage

Run the backend and then launch the Android app. The app communicates with the API under `http://localhost:8000/api/v1`.

## Contributing

Contributions are welcome! Please open issues or pull requests on GitHub. Make sure to format code and provide tests where relevant.
