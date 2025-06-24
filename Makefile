# Simple helper targets for backend development

.PHONY: seed test

seed:
	cd backend && python -m app.db.seed

test:
	pip install -r backend/requirements.txt pytest pytest-asyncio
	pytest backend/tests
