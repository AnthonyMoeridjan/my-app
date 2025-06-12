// src/pages/TransactionForm.tsx
import React, { useEffect, useState, FormEvent, ChangeEvent } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import apiClient from '../services/api'; // Adjust path
import { Transaction, ProjectBasic, TransactionFormData } from '../types'; // Adjust path
import axios from 'axios'; // Import axios for isAxiosError

const initialFormData: TransactionFormData = {
    dagboek: 'Bank',
    date: new Date().toISOString().split('T')[0],
    category: '',
    description: '',
    amount: '',
    btw: '',
    currency: 'SRD',
    projectId: '',
    transactionType: 'CREDIT',
    filePath: '',
    extra: '',
};

const transactionToFormData = (tx: Transaction): TransactionFormData => ({
    dagboek: tx.dagboek || 'Bank',
    date: tx.date ? new Date(tx.date).toISOString().split('T')[0] : '',
    category: tx.category || '',
    description: tx.description || '',
    amount: tx.amount?.toString() || '',
    btw: tx.btw?.toString() || '',
    currency: tx.currency || 'SRD',
    projectId: tx.project?.id?.toString() || '',
    transactionType: tx.transactionType || 'CREDIT',
    filePath: tx.filePath || '',
    extra: tx.extra || '',
});

const TransactionForm: React.FC = () => {
    const { transactionId } = useParams<{ transactionId?: string }>();
    const navigate = useNavigate();
    const [isEditMode, setIsEditMode] = useState(false);
    const [formData, setFormData] = useState<TransactionFormData>(initialFormData);
    const [projects, setProjects] = useState<ProjectBasic[]>([]);
    const [selectedFile, setSelectedFile] = useState<File | null>(null);
    const [filePreview, setFilePreview] = useState<string | null>(null);
    const [isLoading, setIsLoading] = useState(false);
    const [isUploading, setIsUploading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        apiClient.get<ProjectBasic[]>('/projects/all')
            .then(response => setProjects(response.data))
            .catch(err => console.error("Failed to fetch projects:", err));
    }, []);

    useEffect(() => {
        if (transactionId) {
            setIsEditMode(true);
            setIsLoading(true);
            apiClient.get<Transaction>(`/transactions/${transactionId}`)
                .then(response => {
                    setFormData(transactionToFormData(response.data));
                    if (response.data.filePath) {
                        setFilePreview(`/api/v1/files/view/${response.data.filePath}`);
                    } else {
                        setFilePreview(null);
                    }
                })
                .catch(err => {
                    console.error("Failed to fetch transaction:", err);
                    setError("Failed to load transaction details.");
                })
                .finally(() => setIsLoading(false));
        } else {
            setIsEditMode(false);
            setFormData(initialFormData);
            setSelectedFile(null);
            setFilePreview(null);
            setIsLoading(false);
        }
    }, [transactionId]);

    const handleChange = (e: ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleFileChange = (e: ChangeEvent<HTMLInputElement>) => {
        if (e.target.files && e.target.files[0]) {
            const file = e.target.files[0];
            setSelectedFile(file);
            const reader = new FileReader();
            reader.onloadend = () => {
                if (file.type.startsWith("image/")) {
                     setFilePreview(reader.result as string);
                } else {
                    setFilePreview(file.name);
                }
            };
            reader.readAsDataURL(file);
            if (isEditMode && formData.filePath) { // If user selects new file, clear old server filePath from form data
                setFormData(prev => ({ ...prev, filePath: '' }));
            }
        } else { // No file selected or selection cancelled
            setSelectedFile(null);
            // If was edit mode and had a file path, restore it for preview, else null
            setFilePreview(isEditMode && formData.filePath ? `/api/v1/files/view/${formData.filePath}` : null);
        }
    };

    const handleFileUpload = async (): Promise<string | null> => {
        if (!selectedFile) return formData.filePath || null;
        setIsUploading(true); setError(null);
        const fileData = new FormData();
        fileData.append('file', selectedFile);
        try {
            const response = await apiClient.post<{ fileName: string }>('/files/upload', fileData, {
                headers: { 'Content-Type': 'multipart/form-data' },
            });
            setSelectedFile(null);
            setFilePreview(`/api/v1/files/view/${response.data.fileName}`);
            setIsUploading(false);
            return response.data.fileName;
        } catch (err) {
            console.error("File upload failed:", err);
            setError("File upload failed. Please ensure the file is valid and try again.");
            setIsUploading(false);
            return null;
        }
    };

    const handleDeleteFile = async () => {
        const fileToDelete = formData.filePath; // Path of the file currently on server (if any)
        const localFileSelected = !!selectedFile;

        if (!fileToDelete && !localFileSelected) {
            alert("No file to delete."); return;
        }
        if (!window.confirm("Are you sure you want to remove the current attachment?")) return;

        // If there's a file on the server associated with this transaction
        if (isEditMode && fileToDelete && transactionId) {
            setIsLoading(true); setError(null);
            try {
                await apiClient.delete(`/transactions/${transactionId}/attachment`);
                setFormData(prev => ({ ...prev, filePath: '' }));
                setFilePreview(null);
                setSelectedFile(null); // Clear any local selection too
                alert("Attachment removed successfully from server.");
            } catch (err) {
                setError("Failed to delete server attachment.");
            } finally {
                setIsLoading(false);
            }
        } else if (localFileSelected) { // If only a local file is selected (not yet uploaded with transaction)
            setSelectedFile(null);
            setFilePreview(null);
            // formData.filePath should already be empty or will be overwritten by new upload
            setFormData(prev => ({ ...prev, filePath: '' }));
        }
    };

    const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault();
        setError(null);
        let currentFilePath = formData.filePath;

        if (selectedFile) { // If a new file has been selected, it needs to be uploaded
            const uploadedName = await handleFileUpload();
            if (uploadedName) {
                currentFilePath = uploadedName;
            } else {
                 // File upload failed, error is set by handleFileUpload, so stop submission
                return;
            }
        }

        setIsLoading(true);
        const payload = {
            ...formData,
            amount: parseFloat(formData.amount) || 0, // Ensure it's a number
            btw: formData.btw ? parseFloat(formData.btw) : undefined,
            projectId: formData.projectId ? parseInt(formData.projectId, 10) : null,
            filePath: currentFilePath || undefined, // Use the determined file path
        };
        // Clean payload: remove empty optional string fields if backend expects them to be absent
        if (!payload.description) delete (payload as any).description;
        if (payload.btw === undefined) delete (payload as any).btw;
        if (payload.projectId === null) delete (payload as any).projectId;
        if (payload.filePath === undefined) delete (payload as any).filePath;
        if (!payload.extra) delete (payload as any).extra;
        if (!payload.dagboek) delete (payload as any).dagboek;


        try {
            if (isEditMode && transactionId) {
                await apiClient.put(`/transactions/${transactionId}`, payload);
            } else {
                await apiClient.post('/transactions', payload);
            }
            alert(`Transaction ${isEditMode ? 'updated' : 'created'} successfully!`);
            navigate('/transactions');
        } catch (err: any) {
            const errorData = err.response?.data;
            let specificMessage = `Failed to ${isEditMode ? 'update' : 'create'} transaction.`;
            if (errorData && errorData.errors && Array.isArray(errorData.errors)) {
                specificMessage = errorData.errors.map((e: any) => e.defaultMessage || `${e.field}: ${e.code}`).join('; ');
            } else if (errorData && errorData.message) {
                specificMessage = errorData.message;
            } else if (err.message) {
                specificMessage = err.message;
            }
            setError(specificMessage);
        } finally {
            setIsLoading(false);
        }
    };

    if (isLoading && isEditMode && !formData.category && !error) {
        return <p>Loading transaction details...</p>;
    }

    const formRowStyle: React.CSSProperties = { display: 'flex', gap: '20px', marginBottom: '15px' };
    const formFieldStyle: React.CSSProperties = { flex: 1 };
    const labelStyle: React.CSSProperties = { display: 'block', marginBottom: '5px', fontWeight: 500 };
    const inputStyle: React.CSSProperties = { width: '100%', padding: '10px', border: '1px solid #ccc', borderRadius: '4px', boxSizing: 'border-box' };
    const buttonStyle: React.CSSProperties = { padding: '10px 15px', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', fontSize: '1rem' };

    return (
        <div style={{ maxWidth: '700px', margin: '20px auto', padding: '20px', border: '1px solid #ddd', borderRadius: '8px', boxShadow: '0 2px 4px rgba(0,0,0,0.1)' }}>
            <h2>{isEditMode ? `Edit Transaction (ID: ${transactionId})` : 'Create New Transaction'}</h2>
            {error && <p style={{ color: 'red', border: '1px solid red', padding: '10px', marginBottom: '15px', whiteSpace: 'pre-wrap' }}>{error}</p>}

            <form onSubmit={handleSubmit}>
                <div style={formRowStyle}>
                    <div style={formFieldStyle}>
                        <label htmlFor="transactionType" style={labelStyle}>Type *</label>
                        <select id="transactionType" name="transactionType" value={formData.transactionType} onChange={handleChange} required style={inputStyle}>
                            <option value="CREDIT">Inkomsten (Credit)</option>
                            <option value="DEBIT">Uitgaven (Debit)</option>
                        </select>
                    </div>
                    <div style={formFieldStyle}>
                        <label htmlFor="date" style={labelStyle}>Date *</label>
                        <input type="date" id="date" name="date" value={formData.date} onChange={handleChange} required style={inputStyle} />
                    </div>
                </div>
                <div style={formRowStyle}>
                    <div style={formFieldStyle}>
                        <label htmlFor="dagboek" style={labelStyle}>Dagboek</label>
                        <select id="dagboek" name="dagboek" value={formData.dagboek} onChange={handleChange} style={inputStyle}>
                            <option value="">None</option>
                            <option value="Bank">Bank</option>
                            <option value="Kas">Kas</option>
                        </select>
                    </div>
                    <div style={formFieldStyle}>
                        <label htmlFor="category" style={labelStyle}>Category *</label>
                        <input type="text" id="category" name="category" value={formData.category} onChange={handleChange} required style={inputStyle} placeholder="e.g., Bankkosten, Lening" />
                    </div>
                </div>
                <div style={{ marginBottom: '15px' }}>
                    <label htmlFor="description" style={labelStyle}>Description</label>
                    <textarea id="description" name="description" value={formData.description} onChange={handleChange} style={inputStyle} rows={3}></textarea>
                </div>
                <div style={formRowStyle}>
                    <div style={{...formFieldStyle, flex: 2}}>
                        <label htmlFor="amount" style={labelStyle}>Amount *</label>
                        <input type="number" id="amount" name="amount" value={formData.amount} onChange={handleChange} required step="0.01" style={inputStyle} placeholder="0.00" />
                    </div>
                    <div style={formFieldStyle}>
                        <label htmlFor="currency" style={labelStyle}>Currency *</label>
                        <select id="currency" name="currency" value={formData.currency} onChange={handleChange} required style={inputStyle}>
                            <option value="SRD">SRD</option>
                            <option value="USD">USD</option>
                            <option value="EUR">EUR</option>
                        </select>
                    </div>
                </div>
                 <div style={{ marginBottom: '15px' }}>
                    <label htmlFor="btw" style={labelStyle}>BTW (VAT)</label>
                    <input type="number" id="btw" name="btw" value={formData.btw} onChange={handleChange} step="0.01" style={inputStyle} placeholder="0.00" />
                </div>
                <div style={{ marginBottom: '15px' }}>
                    <label htmlFor="projectId" style={labelStyle}>Project</label>
                    <select id="projectId" name="projectId" value={formData.projectId} onChange={handleChange} style={inputStyle}>
                        <option value="">None</option>
                        {projects.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}
                    </select>
                </div>
                <div style={{ marginBottom: '15px' }}>
                    <label htmlFor="file" style={labelStyle}>Attachment</label>
                    <input type="file" id="file" name="file" onChange={handleFileChange} style={{...inputStyle, paddingBottom: '10px'}} accept="image/jpeg,image/png,application/pdf" />
                    {filePreview && (
                        <div style={{ marginTop: '10px' }}>
                            <strong>Preview: </strong>
                            {selectedFile?.name || (formData.filePath && formData.filePath.split('/').pop())}
                            {(formData.filePath || selectedFile) && (
                                <button type="button" onClick={handleDeleteFile} disabled={isLoading || isUploading} style={{...buttonStyle, backgroundColor: '#dc3545', marginLeft: '10px', padding: '5px 10px', fontSize: '0.8em'}}>
                                    Remove Attachment
                                </button>
                            )}
                             {filePreview.startsWith('data:image') || (filePreview.includes('.jpg') || filePreview.includes('.png')) ? (
                                <img src={filePreview} alt="Preview" style={{ maxWidth: '200px', maxHeight: '200px', display: 'block', marginTop: '5px', border: '1px solid #eee' }} />
                            ) : filePreview.includes('.pdf') && filePreview.startsWith('/api/v1/files/view/') ? ( // Only show iframe for server files
                                <iframe src={filePreview} title="PDF Preview" width="100%" height="300px" style={{border: '1px solid #ccc', marginTop: '5px'}}/>
                            ) : null}
                        </div>
                    )}
                </div>
                <div style={{ marginBottom: '20px' }}>
                    <label htmlFor="extra" style={labelStyle}>Extra Info</label>
                    <input type="text" id="extra" name="extra" value={formData.extra} onChange={handleChange} style={inputStyle} />
                </div>
                <div>
                    <button type="submit" disabled={isLoading || isUploading} style={{ ...buttonStyle, backgroundColor: '#007bff' }}>
                        {isUploading ? 'Uploading...' : isLoading ? (isEditMode ? 'Saving...' : 'Creating...') : (isEditMode ? 'Save Changes' : 'Create Transaction')}
                    </button>
                    <button type="button" onClick={() => navigate('/transactions')} disabled={isLoading || isUploading} style={{ ...buttonStyle, backgroundColor: '#6c757d', marginLeft: '10px' }}>
                        Cancel
                    </button>
                </div>
            </form>
        </div>
    );
};
export default TransactionForm;
