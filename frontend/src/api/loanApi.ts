const API_URL = 'http://localhost:8080/api/loan/decision';

export interface LoanRequest {
    personalCode: string;
    loanAmount: number;
    loanPeriod: number;
}

export interface LoanResponse {
    approved: boolean;
    approvedAmount: number;
    approvedPeriod: number;
    message: string;
}

export async function submitLoanRequest(request: LoanRequest): Promise<LoanResponse> {
    const response = await fetch(API_URL, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(request)
    });

    if (!response.ok) {
        const error = await response.json();
        throw new Error(error.error || 'Something went wrong.');
    }

    return response.json();
}