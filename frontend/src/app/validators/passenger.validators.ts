import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/** First/Last name: A-Z or a-z, length 1..20. */
export function nameValidator(): ValidatorFn {
  const re = /^[A-Za-z]{1,20}$/;
  return (ctrl: AbstractControl): ValidationErrors | null => {
    const v = ctrl.value;
    if (v === null || v === undefined || v === '') return null; // let `required` handle emptiness
    return re.test(v) ? null : { invalidName: true };
  };
}

/** Nationality: exactly 3 uppercase letters A-Z. */
export function nationalityValidator(): ValidatorFn {
  const re = /^[A-Z]{3}$/;
  return (ctrl: AbstractControl): ValidationErrors | null => {
    const v = ctrl.value;
    if (v === null || v === undefined || v === '') return null;
    return re.test(v) ? null : { invalidNationality: true };
  };
}

/** Gender: one of Male/Female/Unknown. */
export function genderValidator(): ValidatorFn {
  const allowed = new Set(['Male', 'Female', 'Unknown']);
  return (ctrl: AbstractControl): ValidationErrors | null => {
    const v = ctrl.value;
    if (v === null || v === undefined || v === '') return null;
    return allowed.has(v) ? null : { invalidGender: true };
  };
}

/**
 * Date of birth: must be a real calendar date in dd/MM/yyyy or yyyy-MM-dd form
 * and must be <= today.
 */
export function dateOfBirthValidator(): ValidatorFn {
  return (ctrl: AbstractControl): ValidationErrors | null => {
    const raw = ctrl.value;
    if (!raw) return null;

    const parsed = parseDate(raw);
    if (!parsed) return { invalidDate: true };

    const today = new Date();
    today.setHours(0, 0, 0, 0);
    if (parsed.getTime() > today.getTime()) return { invalidDate: true };

    return null;
  };
}

/**
 * Flight no: 2 alphanumerics (A-Z or 0-9) + 1-4 digits.
 * Implemented as the spec reads.
 */
export function flightNoValidator(): ValidatorFn {
  const re = /^[A-Z0-9]{2}[0-9]{1,4}$/;
  return (ctrl: AbstractControl): ValidationErrors | null => {
    const v = ctrl.value;
    if (v === null || v === undefined || v === '') return null;
    return re.test(v) ? null : { invalidFlightNo: true };
  };
}

/** Returns a valid Date, or null if `raw` isn't a real calendar date. */
export function parseDate(raw: string): Date | null {
  // Accepted formats: dd/MM/yyyy, dd-MM-yyyy, yyyy-MM-dd
  let d: number | null = null;
  let m: number | null = null;
  let y: number | null = null;

  let match = /^(\d{1,2})[\/\-](\d{1,2})[\/\-](\d{4})$/.exec(raw);
  if (match) {
    d = +match[1]!;
    m = +match[2]!;
    y = +match[3]!;
  } else {
    match = /^(\d{4})-(\d{1,2})-(\d{1,2})$/.exec(raw);
    if (match) {
      y = +match[1]!;
      m = +match[2]!;
      d = +match[3]!;
    }
  }

  if (d === null || m === null || y === null) return null;

  const dt = new Date(y, m - 1, d);
  if (
    dt.getFullYear() !== y ||
    dt.getMonth() !== m - 1 ||
    dt.getDate() !== d
  ) {
    return null; // e.g. 31/02/2024 would roll over
  }
  return dt;
}
