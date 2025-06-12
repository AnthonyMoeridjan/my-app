export interface ProjectBasic {
    id: number;
    name: string;
}

export interface Transaction {
    id: number;
    dagboek?: string | null;
    date: string; // "YYYY-MM-DD" from backend, can be Date object if transformed
    category: string;
    description?: string | null;
    amount: number;
    btw?: number | null;
    currency?: string | null;
    project?: ProjectBasic | null; // Assuming project is simplified here
    transactionType: 'DEBIT' | 'CREDIT';
    filePath?: string | null;
    extra?: string | null;
    createdAt?: string; // Or Date
    updatedAt?: string; // Or Date
}

export interface PaginatedTransactionsResponse {
    content: Transaction[];
    totalPages: number;
    totalElements: number;
    number: number; // Current page (0-indexed)
    size: number;
    first: boolean;
    last: boolean;
    empty: boolean;
}

export interface TransactionFilters {
    startDate?: string | null;
    endDate?: string | null;
    type?: 'DEBIT' | 'CREDIT' | '';
    projectId?: number | string | null; // string for '', number for id
    dagboek?: string | null;
    category?: string | null;
    description?: string | null;
    extra?: string | null;
}

export interface TransactionTotals {
    SRD: number;
    USD: number;
    EUR: number;
}

// This is the data shape for the form state
export interface TransactionFormData {
    dagboek: string;
    date: string; // "YYYY-MM-DD" for input type="date"
    category: string;
    description: string;
    amount: string; // string from input, convert to number on submit
    btw: string;    // string from input, convert to number on submit
    currency: string;
    projectId: string; // string from select, convert to number or null on submit
    transactionType: 'CREDIT' | 'DEBIT';
    filePath: string; // Store the path/filename returned by upload API
    extra: string;
    // lener?: string; // Add if implementing the lener field
}
