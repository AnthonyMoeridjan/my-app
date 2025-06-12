import React, { useEffect, useState, useCallback } from 'react';
import apiClient from '../services/api'; // Adjust path
import { Link } from 'react-router-dom';
import { Transaction, PaginatedTransactionsResponse, TransactionFilters, TransactionTotals } from '../types'; // Adjust path
import TransactionFiltersComponent from '../components/transactions/TransactionFilters'; // Adjust path

const buildQueryString = (filters: TransactionFilters, page: number, size: number, sort: string = 'date,desc'): string => {
    const params = new URLSearchParams();
    Object.entries(filters).forEach(([key, value]) => {
        if (value !== null && value !== undefined && String(value).trim() !== '') {
            params.append(key, String(value));
        }
    });
    params.append('page', String(page));
    params.append('size', String(size));
    params.append('sort', sort);
    return params.toString();
};

const TransactionListPage: React.FC = () => {
    const [transactions, setTransactions] = useState<Transaction[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [pageSize, setPageSize] = useState(10);
    const [sort, setSort] = useState<string>('date,desc');
    const initialFiltersState: TransactionFilters = {
        startDate: undefined, endDate: undefined, type: '', projectId: undefined,
        dagboek: undefined, category: undefined, description: undefined, extra: undefined
    };
    const [activeFilters, setActiveFilters] = useState<TransactionFilters>(initialFiltersState);
    const [totals, setTotals] = useState<TransactionTotals | null>(null);

    const fetchTransactionsAndTotals = useCallback(async (filters: TransactionFilters, page: number, pSize: number, srt: string) => {
        setIsLoading(true);
        setError(null);
        const queryString = buildQueryString(filters, page, pSize, srt);
        try {
            const [transactionsResponse, totalsResponse] = await Promise.all([
                apiClient.get<PaginatedTransactionsResponse>(`/transactions?${queryString}`),
                apiClient.get<TransactionTotals>(`/transactions/totals?${queryString}`)
            ]);
            setTransactions(transactionsResponse.data.content);
            setTotalPages(transactionsResponse.data.totalPages);
            setCurrentPage(transactionsResponse.data.number);
            setPageSize(transactionsResponse.data.size);
            setTotals(totalsResponse.data);
        } catch (err) {
            const errorMessage = (err instanceof Error) ? err.message : 'Failed to fetch data.';
            setError(errorMessage);
            setTransactions([]); setTotals(null);
        } finally {
            setIsLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchTransactionsAndTotals(activeFilters, currentPage, pageSize, sort);
    }, [fetchTransactionsAndTotals, activeFilters, currentPage, pageSize, sort]);

    const handleApplyFilters = (newFilters: TransactionFilters) => {
        setActiveFilters(newFilters);
        setCurrentPage(0);
    };
    const handleClearFilters = () => {
        setActiveFilters(initialFiltersState);
        setCurrentPage(0);
    };
    const handlePageChange = (newPage: number) => {
        if (newPage >= 0 && newPage < totalPages) setCurrentPage(newPage);
    };
    const handleSort = (columnKey: string) => {
        const [currentSortCol, currentSortDir = 'asc'] = sort.split(',');
        let newSortDir = 'asc';
        if (columnKey === currentSortCol && currentSortDir === 'asc') newSortDir = 'desc';
        setSort(`${columnKey},${newSortDir}`);
        setCurrentPage(0);
    };
    const handleDeleteTransaction = async (transactionId: number) => {
        if (!window.confirm(`Delete transaction ID ${transactionId}?`)) return;
        try {
            await apiClient.delete(`/transactions/${transactionId}`);
            fetchTransactionsAndTotals(activeFilters, currentPage, pageSize, sort);
        } catch (err) { setError((err instanceof Error) ? err.message : 'Failed to delete.'); }
    };

    const tableHeaderStyle: React.CSSProperties = { cursor: 'pointer', userSelect: 'none', borderBottom: '2px solid #dee2e6', padding: '0.75rem', textAlign: 'left', backgroundColor: '#f8f9fa' };
    const tableCellStyle: React.CSSProperties = { borderBottom: '1px solid #dee2e6', padding: '0.75rem', verticalAlign: 'top' };
    const totalCardStyle: React.CSSProperties = { padding: '10px', border: '1px solid #ddd', borderRadius: '4px', margin: '0 5px', backgroundColor: '#f9f9f9', minWidth: '100px', textAlign: 'center'};

    const getSortIndicator = (columnKey: string) => {
        const [currentSortCol, currentSortDir = 'asc'] = sort.split(',');
        if (columnKey === currentSortCol) return currentSortDir === 'asc' ? ' ▲' : ' ▼';
        return '';
    };

    return (
        <div style={{ padding: '20px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                <h2>Transactions</h2>
                <Link to="/transactions/new" style={{ padding: '0.5rem 1rem', backgroundColor: '#28a745', color: 'white', textDecoration: 'none', borderRadius: '4px' }}>
                    New Transaction
                </Link>
            </div>
            <TransactionFiltersComponent initialFilters={activeFilters} onApplyFilters={handleApplyFilters} onClearFilters={handleClearFilters} />
            {totals && !isLoading && (
                <div style={{ display: 'flex', justifyContent: 'space-around', marginBottom: '20px', padding: '10px', backgroundColor: '#e9ecef', borderRadius: '5px' }}>
                    <div style={totalCardStyle}><strong>SRD:</strong> {totals.SRD?.toFixed(2) || '0.00'}</div>
                    <div style={totalCardStyle}><strong>USD:</strong> {totals.USD?.toFixed(2) || '0.00'}</div>
                    <div style={totalCardStyle}><strong>EUR:</strong> {totals.EUR?.toFixed(2) || '0.00'}</div>
                </div>
            )}
            {isLoading && <p>Loading...</p>}
            {error && <p style={{ color: 'red' }}>Error: {error}</p>}
            {!isLoading && !error && transactions.length === 0 && <p>No transactions found.</p>}
            {!isLoading && !error && transactions.length > 0 && (
                <>
                    <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.9em' }}>
                        <thead>
                            <tr>
                                <th style={tableHeaderStyle} onClick={() => handleSort('date')}>Date{getSortIndicator('date')}</th>
                                <th style={tableHeaderStyle} onClick={() => handleSort('dagboek')}>Dagboek{getSortIndicator('dagboek')}</th>
                                <th style={tableHeaderStyle} onClick={() => handleSort('category')}>Category{getSortIndicator('category')}</th>
                                <th style={tableHeaderStyle}>Description</th>
                                <th style={tableHeaderStyle} onClick={() => handleSort('amount')}>Amount{getSortIndicator('amount')}</th>
                                <th style={tableHeaderStyle}>Currency</th>
                                <th style={tableHeaderStyle} onClick={() => handleSort('transactionType')}>Type{getSortIndicator('transactionType')}</th>
                                <th style={tableHeaderStyle} onClick={() => handleSort('project.name')}>Project{getSortIndicator('project.name')}</th>
                                <th style={tableHeaderStyle}>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {transactions.map(tx => (
                                <tr key={tx.id}>
                                    <td style={tableCellStyle}>{new Date(tx.date).toLocaleDateString()}</td>
                                    <td style={tableCellStyle}>{tx.dagboek}</td>
                                    <td style={tableCellStyle}>{tx.category}</td>
                                    <td style={tableCellStyle}>{tx.description}</td>
                                    <td style={tableCellStyle}>{tx.amount.toFixed(2)}</td>
                                    <td style={tableCellStyle}>{tx.currency}</td>
                                    <td style={tableCellStyle}>{tx.transactionType}</td>
                                    <td style={tableCellStyle}>{tx.project?.name || '-'}</td>
                                    <td style={tableCellStyle}>
                                        <Link to={`/transactions/${tx.id}/edit`} style={{ marginRight: '8px' }}>Edit</Link>
                                        <button onClick={() => handleDeleteTransaction(tx.id)}>Delete</button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                    <div style={{ marginTop: '1rem', display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
                        <button onClick={() => handlePageChange(currentPage - 1)} disabled={currentPage === 0 || isLoading}>Previous</button>
                        <span style={{ margin: '0 1rem' }}>Page {currentPage + 1} of {totalPages || 1}</span>
                        <button onClick={() => handlePageChange(currentPage + 1)} disabled={currentPage >= totalPages - 1 || totalPages === 0 || isLoading}>Next</button>
                    </div>
                </>
            )}
        </div>
    );
};
export default TransactionListPage;
