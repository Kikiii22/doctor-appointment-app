# Medical Department Similarity API

This API uses **sentence embeddings** to determine which medical department is most relevant for a given patient symptom description. It is built with **FastAPI** and **SentenceTransformers**.

---

## Features

- Compare a symptom sentence to predefined medical departments.
- Returns a **normalized similarity score** for each department.
- Health check endpoint for monitoring.
- Ready to run locally.

---

## Requirements

The app depends on the following Python packages:

- fastapi
- uvicorn
- pydantic
- sentence-transformers
- torch
- transformers
- huggingface-hub
- numpy
- scipy

Install all dependencies using:

```bash
pip install -r requirements.txt
```

**Activate virtual environment, install requirements, run** :

```bash
python -m venv venv
source venv/bin/activate

pip install -r requirements.txt

uvicorn test:app --reload


```
