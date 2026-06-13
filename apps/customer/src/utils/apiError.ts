import type { ApiErrorResponse, FieldValidationError } from '../types/api';

export function fieldErrorsFromApi(errors?: FieldValidationError[]): Record<string, string> {
  if (!errors?.length) return {};
  return Object.fromEntries(errors.map((error) => [error.field, error.message]));
}

export function formatApiErrorMessage(body: Pick<ApiErrorResponse, 'message' | 'errors'>): string {
  if (body.errors?.length) {
    return body.errors.map((error) => error.message).join(' · ');
  }
  return body.message || 'Something went wrong';
}

export async function readApiErrorResponse(res: Response): Promise<ApiErrorResponse> {
  try {
    return (await res.json()) as ApiErrorResponse;
  } catch {
    return { success: false, message: `Request failed (${res.status})` };
  }
}

function formatLocalDate(date: Date): string {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

/** Tomorrow in YYYY-MM-DD — matches backend @Future on check-in date. */
export function minCheckInDate(): string {
  const date = new Date();
  date.setDate(date.getDate() + 1);
  return formatLocalDate(date);
}

/** Day after check-in, or tomorrow if check-in is empty. */
export function minCheckOutDate(checkIn: string): string {
  if (!checkIn) return minCheckInDate();
  const [year, month, day] = checkIn.split('-').map(Number);
  const date = new Date(year, month - 1, day);
  date.setDate(date.getDate() + 1);
  return formatLocalDate(date);
}
