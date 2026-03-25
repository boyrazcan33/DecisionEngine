import { submitLoanRequest, LoanResponse } from './api/loanApi.js';

const form = document.getElementById('loan-form') as HTMLFormElement;
const resultDiv = document.getElementById('result') as HTMLDivElement;

form.addEventListener('submit', async (e) => {
    e.preventDefault();

    const personalCode = (document.getElementById('personalCode') as HTMLInputElement).value;
    const loanAmount = parseInt((document.getElementById('loanAmount') as HTMLInputElement).value);
    const loanPeriod = parseInt((document.getElementById('loanPeriod') as HTMLInputElement).value);

    try {
        const response: LoanResponse = await submitLoanRequest({ personalCode, loanAmount, loanPeriod });
        showResult(response);
    } catch (error: any) {
        showError(error.message);
    }
});

function showResult(response: LoanResponse): void {
    resultDiv.className = response.approved ? 'result approved' : 'result rejected';
    resultDiv.innerHTML = `
        <h3>${response.approved ? '✅ Approved' : '❌ Rejected'}</h3>
        <p>${response.message}</p>
        ${response.approved ? `
            <p>Amount: <strong>€${response.approvedAmount}</strong></p>
            <p>Period: <strong>${response.approvedPeriod} months</strong></p>
        ` : ''}
    `;
}

function showError(message: string): void {
    resultDiv.className = 'result error';
    resultDiv.innerHTML = `<p>⚠️ ${message}</p>`;
}