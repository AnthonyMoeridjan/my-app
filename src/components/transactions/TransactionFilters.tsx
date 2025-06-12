import React, { useEffect, useState } from 'react';
import { TransactionFilters, ProjectBasic } from '../../types'; // Adjust path if your types are elsewhere
import apiClient from '../../services/api'; // Adjust path

interface TransactionFiltersProps {
    initialFilters: TransactionFilters;
    onApplyFilters: (filters: TransactionFilters) => void;
    onClearFilters: () => void; // Add this prop
}

const TransactionFiltersComponent: React.FC<TransactionFiltersProps> = ({ initialFilters, onApplyFilters, onClearFilters }) => {
    const [filters, setFilters] = useState<TransactionFilters>(initialFilters);
    const [projects, setProjects] = useState<ProjectBasic[]>([]);

    useEffect(() => {
        apiClient.get<ProjectBasic[]>('/projects/all')
            .then(response => setProjects(response.data))
            .catch(error => console.error("Failed to fetch projects for filter:", error));
    }, []);

    useEffect(() => {
        setFilters(initialFilters);
    }, [initialFilters]);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
        const { name, value } = e.target;
        setFilters(prev => ({ ...prev, [name]: value }));
    };

    const handleApply = (e: React.FormEvent) => {
        e.preventDefault();
        onApplyFilters(filters);
    };

    const handleClear = (e: React.FormEvent) => {
        e.preventDefault();
        const clearedFilters: TransactionFilters = { startDate: undefined, endDate: undefined, type: '', projectId: undefined, dagboek: undefined, category: undefined, description: undefined, extra: undefined };
        setFilters(clearedFilters); // Reset local state
        onClearFilters(); // Call parent's clear handler
    };

    const inputGroupStyle: React.CSSProperties = { marginBottom: '10px', marginRight: '10px', flex: '1 1 200px', minWidth: '180px' };
    const labelStyle: React.CSSProperties = { display: 'block', marginBottom: '4px', fontSize: '0.9em', fontWeight: 500 };
    const inputStyle: React.CSSProperties = { width: '100%', padding: '8px', boxSizing: 'border-box', border: '1px solid #ccc', borderRadius: '4px' };

    return (
        <form onSubmit={handleApply} style={{ padding: '15px', border: '1px solid #e0e0e0', borderRadius: '5px', marginBottom: '20px', backgroundColor: '#f9f9f9' }}>
            <h4 style={{marginTop: 0, marginBottom: '15px'}}>Filter Transactions</h4>
            <div style={{ display: 'flex', flexWrap: 'wrap' }}>
                {/* Input fields as described in the main explanation */}
                <div style={inputGroupStyle}>
                    <label htmlFor="startDate" style={labelStyle}>Start Date:</label>
                    <input type="date" name="startDate" id="startDate" value={filters.startDate || ''} onChange={handleChange} style={inputStyle} />
                </div>
                <div style={inputGroupStyle}>
                    <label htmlFor="endDate" style={labelStyle}>End Date:</label>
                    <input type="date" name="endDate" id="endDate" value={filters.endDate || ''} onChange={handleChange} style={inputStyle} />
                </div>
                <div style={inputGroupStyle}>
                    <label htmlFor="type" style={labelStyle}>Type:</label>
                    <select name="type" id="type" value={filters.type || ''} onChange={handleChange} style={inputStyle}>
                        <option value="">All Types</option>
                        <option value="CREDIT">Inkomsten (Credit)</option>
                        <option value="DEBIT">Uitgaven (Debit)</option>
                    </select>
                </div>
                <div style={inputGroupStyle}>
                    <label htmlFor="projectId" style={labelStyle}>Project:</label>
                    <select name="projectId" id="projectId" value={filters.projectId || ''} onChange={handleChange} style={inputStyle}>
                        <option value="">All Projects</option>
                        {projects.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}
                    </select>
                </div>
                <div style={inputGroupStyle}>
                    <label htmlFor="dagboek" style={labelStyle}>Dagboek:</label>
                    <select name="dagboek" id="dagboek" value={filters.dagboek || ''} onChange={handleChange} style={inputStyle}>
                        <option value="">All</option>
                        <option value="Kas">Kas</option>
                        <option value="Bank">Bank</option>
                    </select>
                </div>
                <div style={inputGroupStyle}>
                    <label htmlFor="category" style={labelStyle}>Category:</label>
                    <input type="text" name="category" id="category" value={filters.category || ''} onChange={handleChange} style={inputStyle} placeholder="Contains..."/>
                </div>
                <div style={inputGroupStyle}>
                    <label htmlFor="description" style={labelStyle}>Description:</label>
                    <input type="text" name="description" id="description" value={filters.description || ''} onChange={handleChange} style={inputStyle} placeholder="Contains..."/>
                </div>
                 <div style={inputGroupStyle}>
                    <label htmlFor="extra" style={labelStyle}>Extra Info:</label>
                    <input type="text" name="extra" id="extra" value={filters.extra || ''} onChange={handleChange} style={inputStyle} placeholder="Contains..."/>
                </div>
            </div>
            <div style={{ marginTop: '15px' }}>
                <button type="submit" style={{ marginRight: '10px', padding: '8px 15px', backgroundColor: '#007bff', color: 'white', border: 'none', borderRadius: '4px' }}>Apply Filters</button>
                <button type="button" onClick={handleClear} style={{ padding: '8px 15px', backgroundColor: '#6c757d', color: 'white', border: 'none', borderRadius: '4px' }}>Clear Filters</button>
            </div>
        </form>
    );
};
export default TransactionFiltersComponent;
