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

To use the AI features you need an OpenRouter account. Sign up at
[OpenRouter](https://openrouter.ai) and generate an API key from the dashboard.
Example models that work well are `mistralai/mistral-7b-instruct` for the planner
and `google/gemma-7b-it` for the generator.

Create a `.env` file inside the `backend/` directory and define these variables:

```bash
OPENROUTER_API_KEY=<your-key>
PLANNER_MODEL_NAME=mistralai/mistral-7b-instruct
GENERATOR_MODEL_NAME=google/gemma-7b-it
APP_SITE_URL=https://yourdomain.com
APP_NAME=Dear Diary
```

## Usage


Run the backend and then launch the Android app. The base URL used by the Android app is provided via `BuildConfig`. Debug builds point to `http://10.0.2.2:8000/api/v1/` so the emulator can reach your machine, while release builds default to `https://yourdomain.com/api/v1/`.

### Android Usage

Long‑press any message in the conversation to enter selection mode. A delete icon will appear in the top bar so you can remove the selected messages, similar to how WhatsApp handles chat message deletion.

### Authentication workflow

Before using the chat endpoint you must create an account and obtain an access token:

1. Register:
   ```bash
   curl -X POST http://localhost:8000/api/v1/auth/register \
        -H "Content-Type: application/json" \
        -d '{"username": "test", "email": "test@example.com", "password": "secret"}'
   ```
2. Login and store the returned `access_token`:
   ```bash
   curl -X POST http://localhost:8000/api/v1/auth/login \
        -H "Content-Type: application/json" \
        -d '{"email": "test@example.com", "password": "secret"}'
   ```
3. Optionally verify the token by fetching the current user:
   ```bash
   curl http://localhost:8000/api/v1/users/me \
        -H "Authorization: Bearer <token>"
   ```
4. Use the token when calling `/chat/`:
   ```bash
   curl -X POST http://localhost:8000/api/v1/chat/ \
        -H "Authorization: Bearer <token>" \
        -H "Content-Type: application/json" \
        -d '{"message": "Halo"}'
   ```

The chat endpoint performs a basic emotion analysis on each message. The detected label is saved with the chat history and influences the AI planner and generator prompts.

## Running Tests

```bash
pip install -r backend/requirements.txt
pip install pytest pytest-asyncio
pytest backend/tests
```

## Contributing

Contributions are welcome! Please open issues or pull requests on GitHub. Make sure to format code and provide tests where relevant.

## License

This project is licensed under the [MIT License](LICENSE).

