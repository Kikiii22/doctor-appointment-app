from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, field_validator
from sentence_transformers import SentenceTransformer, util

app = FastAPI()

departments = {
    "Cardiology": [
        "Disorders of the heart and blood vessels, including arrhythmias, heart attacks, chest pain, heart failure, and hypertension.",
        "Diagnosis, treatment, and management of cardiovascular diseases.",
        "Symptoms like palpitations, shortness of breath, dizziness, fainting, fatigue, and chest discomfort are evaluated by cardiologists."
    ],
    "Neurology": [
        "Neurology focuses on disorders of the nervous system including the brain, spinal cord, nerves, and muscles.",
        "Symptoms such as dizziness, headaches, seizures, numbness, weakness, tremors, or memory loss are assessed and treated by neurologists.",
        "Neurologists manage conditions like stroke, neuropathy, multiple sclerosis, and neuropathic pain."
    ],
    "Endocrinology": [
        "Endocrinology deals with hormones and endocrine system disorders.",
        "Common conditions include diabetes, thyroid disorders, adrenal gland disorders, and hormonal imbalances.",
        "Symptoms include fatigue, weight changes, excessive thirst, frequent urination, hair loss, and mood swings."
    ],
    "Psychiatry": [
        "Psychiatry involves the diagnosis and treatment of mental illnesses, emotional disturbances, and behavioral disorders.",
        "Psychiatrists address conditions like depression, anxiety, bipolar disorder, schizophrenia, and stress-related disorders.",
        "Symptoms include persistent sadness, mood swings, confusion, hallucinations, insomnia, and social withdrawal. Therapy, medication, and counseling are used to improve mental health."
    ],
    "Dermatology": [
        "Dermatology deals with skin, hair, and nail conditions.",
        "Dermatologists treat acne, eczema, psoriasis, rashes, infections, and skin cancer.",
        "Symptoms include itching, redness, lesions, hair loss, nail changes, and skin irritation. They also provide cosmetic treatments and manage allergies."
    ],
    "Gastroenterology": [
        "Gastroenterology focuses on the digestive system.",
        "Gastroenterologists treat conditions of the stomach, intestines, liver, pancreas, and gallbladder.",
        "Symptoms like abdominal pain, bloating, nausea, constipation, and diarrhea are evaluated and managed.",
        "Symptoms include nausea, vomiting, diarrhea, constipation, bloating, and abdominal pain."
    ],
    "Infectology": [
        "Infectology deals with acute and chronic infectious diseases caused by bacteria, viruses, fungi, and parasites.",
        "Infectologists diagnose and treat infections such as influenza, tuberculosis, HIV, and sepsis.",
        "They also provide vaccination guidance and infection prevention strategies.",
        "Symptoms include vomiting, cough, high temperature, sore throat, and clogged nose."
    ],
    "Toxicology": [
        "Toxicology focuses on illnesses caused by exposure to chemicals, drugs, and environmental toxins.",
        "Toxicologists treat poisoning, chemical exposure, drug overdoses, and adverse reactions.",
        "They evaluate risk factors and recommend detoxification or supportive treatments.",
        "Symptoms could be caused by alcohol consumption, drug consumption, chemical accident."
    ],
    "Orthopedics": [
        "Orthopedics deals with the musculoskeletal system including bones, joints, ligaments, muscles, and tendons.",
        "Orthopedic surgeons treat fractures, arthritis, sports injuries, spine disorders, and congenital deformities.",
        "They also perform surgical and non-surgical interventions to restore mobility and function.",
        "Symptoms include joint pain, bone pain, muscle pain, and could be caused by falling, accidents, harmful behavior."
    ]
}


class SentenceRequest(BaseModel):
    sentence: str

    @field_validator('sentence')
    @classmethod
    def sentence_must_not_be_empty(cls, v):
        if not v or not v.strip():
            raise ValueError('Sentence cannot be empty')
        return v.strip()[:500]


model = SentenceTransformer("all-MiniLM-L6-v2")

department_embeddings = {}
for dept, texts in departments.items():
    combined_text = " ".join(texts)
    department_embeddings[dept] = model.encode(
        combined_text, convert_to_tensor=True)


@app.post("/embedding/similarity")
def similarity(req: SentenceRequest):
    try:
        input_embedding = model.encode(req.sentence, convert_to_tensor=True)
        scores = {}

        for dept, dept_emb in department_embeddings.items():
            sim_score = util.cos_sim(input_embedding, dept_emb).item()
            scores[dept] = sim_score

        max_score = max(scores.values()) if scores else 1
        if max_score > 0:
            scores = {dept: score/max_score for dept, score in scores.items()}

        return scores

    except Exception as e:
        raise HTTPException(
            status_code=500, detail=f"Similarity calculation failed: {str(e)}"
        )


@app.get("/health")
def health_check():
    return {"status": "healthy", "mode": "embedding_similarity", "message": "Service running locally"}


@app.get("/")
def root():
    return {"message": "Medical Department Similarity API", "mode": "embedding_similarity", "dependencies": "none"}
