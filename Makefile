# Simple helper targets for backend development

.PHONY: seed

seed:
	cd backend && python -m app.db.seed
