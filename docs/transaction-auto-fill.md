# Transaction Auto-Fill Strategy

This document outlines a suggested approach for implementing an AI-powered auto-fill feature in the `TransactionEditView` when users upload receipts in image or PDF formats.

## 1. High-Level Architecture

1. **Client Uploads File**
   - Extend the Vaadin `Upload` component in `TransactionEditView` to accept images and PDFs.
   - Persist the uploaded file to temporary storage (e.g., filesystem, S3, database blob) and forward a reference to the backend service layer.

2. **Document Ingestion Service**
   - Create a Spring service responsible for orchestrating AI extraction. Responsibilities include:
     - Validating file type/size.
     - Converting PDFs to images (one image per page) when required (Apache PDFBox, PDF.js).
     - Running OCR (e.g., Tesseract, AWS Textract, Google Document AI, Azure Form Recognizer).
     - Normalizing the raw text output.

3. **Information Extraction Layer**
   - Implement field extraction either via:
     - **Rules/heuristics** (regex patterns for dates, totals, tax, vendor name) if documents are consistent.
     - **LLM prompting** (OpenAI GPT-4o mini, Anthropic Claude) using structured prompts with function-calling/JSON schema to request `Transaction` fields.
     - **Fine-tuned model** if a labeled dataset is available.
   - Combine heuristics with LLM validation to boost accuracy.

4. **Mapping to UI**
   - Return a DTO with the detected field values and confidence scores to `TransactionEditView`.
   - Pre-populate the Vaadin form fields while still allowing manual edits.
   - Provide visual feedback on confidence (e.g., color coding or tooltips).

## 2. Detailed Steps

1. **Extend the Upload Component**
   - Listen to `SucceededEvent` to trigger processing.
   - Show a loading indicator while extraction runs asynchronously.

2. **Backend REST Endpoint**
   - Create an endpoint such as `POST /api/transactions/parse` that receives the uploaded file, invokes the document ingestion service, and returns field suggestions.
   - Secure the endpoint (authentication, file size limits, content filtering).

3. **AI Processing Options**
   - **Self-hosted OCR + LLM API**: Use Tesseract for text extraction, then prompt an LLM via OpenAI's Assistants or Responses API with instructions to extract JSON fields (`date`, `amount`, `merchant`, `category`, `notes`).
   - **Managed Form Extraction**: Integrate AWS Textract or Azure Form Recognizer to get structured outputs without custom prompts.
   - **Hybrid Pipeline**: Run managed service first; if confidence is low, fall back to LLM prompting with the raw text snippet.

4. **Data Validation**
   - Apply server-side validation to ensure values make sense (e.g., amounts parse to BigDecimal, date formats).
   - Allow users to accept or override each field individually.

5. **Observability & Feedback**
   - Log extraction results and confidence to monitor accuracy.
   - Provide a "Report issue" mechanism in the UI to collect corrections for future model fine-tuning.

## 3. No Dedicated Agent Required

- A full autonomous agent is not required. A deterministic service that orchestrates OCR and either heuristics or LLM responses suffices.
- Consider using OpenAI's **Responses** API with JSON schema to request structured fields, which keeps control in your backend.
- Only use an agentic workflow if you need multi-step reasoning (e.g., fetching additional context or calling external APIs based on content).

## 4. Security & Compliance

- Sanitize and securely store documents (PII). Encrypt at rest and in transit.
- Respect data retention policies; delete temporary files after processing.
- Implement rate limiting and quota monitoring for external AI APIs.

## 5. Future Enhancements

- Build a training dataset from corrected forms and fine-tune a model or train a supervised classifier.
- Add multi-language OCR support.
- Introduce active learning: ask the user to confirm ambiguous fields, storing both input and correction.

