var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
import { submitLoanRequest } from './api/loanApi.js';
const form = document.getElementById('loan-form');
const resultDiv = document.getElementById('result');
form.addEventListener('submit', (e) => __awaiter(void 0, void 0, void 0, function* () {
    e.preventDefault();
    const personalCode = document.getElementById('personalCode').value;
    const loanAmount = parseInt(document.getElementById('loanAmount').value);
    const loanPeriod = parseInt(document.getElementById('loanPeriod').value);
    try {
        const response = yield submitLoanRequest({ personalCode, loanAmount, loanPeriod });
        showResult(response);
    }
    catch (error) {
        showError(error.message);
    }
}));
function showResult(response) {
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
function showError(message) {
    resultDiv.className = 'result error';
    resultDiv.innerHTML = `<p>⚠️ ${message}</p>`;
}
