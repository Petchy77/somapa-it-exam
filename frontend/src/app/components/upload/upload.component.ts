import { Component, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { PassengerService } from '../../services/passenger.service';
import { ResultStore } from '../../services/result.store';
import { RowError, UploadResponse } from '../../models/passenger.model';
import { flightNoValidator } from '../../validators/passenger.validators';

@Component({
  selector: 'app-upload',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './upload.component.html'
})
export class UploadComponent {
  @ViewChild('hiddenFileInput') hiddenFileInput!: ElementRef<HTMLInputElement>;

  form: FormGroup;
  selectedFile: File | null = null;
  fileSubmitted = false;
  rowErrors: RowError[] = [];
  hasBackendError = false;
  serverMessage = '';
  submitted = false;
  fileNameMismatch = false;
  fileTooLarge = false;

  constructor(
    private fb: FormBuilder,
    private api: PassengerService,
    private router: Router,
    private store: ResultStore
  ) {
    this.form = this.fb.group({
      flightNo: ['', [Validators.required, flightNoValidator()]]
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    this.rowErrors = [];
    this.hasBackendError = false;
    this.serverMessage = '';
    this.fileNameMismatch = false;
    this.fileTooLarge = false;

    if (file && file.size > 1024 * 1024) {
      this.fileTooLarge = true;
      this.selectedFile = null;
      input.value = '';
      return;
    }
    this.selectedFile = file;

    // Auto-fill Flight no if the file name matches `Passenger_{FlightNo}.xlsx`
    // and the user hasn't typed a flight no yet.
    if (file) {
      const match = /^Passenger_([A-Z0-9]{2}[0-9]{1,4})\.xlsx$/.exec(file.name);
      const currentFlightNo = (this.form.value.flightNo || '').trim();
      if (match && !currentFlightNo) {
        this.form.patchValue({ flightNo: match[1] });
      }
    }

    // Reset the native input value AFTER reading the file so that:
    //   - picking the same file again still fires `change`
    //   - works correctly after Cancel/Clear + reselect loops
    input.value = '';
  }

  onSave(): void {
    this.submitted = true;
    this.fileSubmitted = true;
    this.rowErrors = [];
    this.hasBackendError = false;
    this.serverMessage = '';
    this.fileNameMismatch = false;

    this.form.markAllAsTouched();
    if (this.form.invalid || !this.selectedFile) {
      return;
    }

    const flightNo: string = this.form.value.flightNo;

    this.api.upload(flightNo, this.selectedFile).subscribe({
      next: (res: UploadResponse) => {
        if (res.success) {
          this.store.set(flightNo, this.selectedFile!, res.passengers);
          this.router.navigate(['/result']);
        } else {
          this.rowErrors = res.errors;
          this.hasBackendError = true;
        }
      },
      error: (err) => {
        this.hasBackendError = true;

        // Network error: backend ไม่ตอบ / ต่อไม่ได้
        if (!err?.status || err.status === 0) {
          this.serverMessage = 'HTTP 400 Bad Request - Connection failed. Please try again.';
          return;
        }

        // Backend ตอบ error กลับมา
        const statusTextMap: Record<number, string> = {
          400: 'Bad Request',
          404: 'Not Found',
          500: 'Internal Server Error'
        };
        const status = `HTTP ${err.status} ${statusTextMap[err.status] || err.statusText || ''} - `;
        const body = typeof err?.error === 'string'
          ? err.error
          : 'Upload failed. Please try again.';
        this.serverMessage = `${status}${body}`.trim();
      }
    });
  }

  onCancel(): void {
    this.form.reset({ flightNo: '' });
    this.selectedFile = null;
    this.fileSubmitted = false;
    this.submitted = false;
    this.rowErrors = [];
    this.hasBackendError = false;
    this.serverMessage = '';
    this.fileNameMismatch = false;
    this.fileTooLarge = false;
    if (this.hiddenFileInput?.nativeElement) {
      this.hiddenFileInput.nativeElement.value = '';
    }
  }

  get flightNoCtrl() {
    return this.form.get('flightNo')!;
  }

  get expectedFileName(): string {
    const v = this.form.value.flightNo;
    return v ? `Passenger_${v}.xlsx` : '';
  }

  showFlightNoRequired(): boolean {
    return this.submitted && this.flightNoCtrl.hasError('required');
  }

  showFlightNoInvalid(): boolean {
    return (
      this.submitted &&
      !this.flightNoCtrl.hasError('required') &&
      this.flightNoCtrl.hasError('invalidFlightNo')
    );
  }

  showFileRequired(): boolean {
    return this.submitted && !this.selectedFile;
  }
}
