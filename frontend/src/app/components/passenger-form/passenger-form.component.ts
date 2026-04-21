import { Component, EventEmitter, Input, Output, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

import { Passenger } from '../../models/passenger.model';
import {
  dateOfBirthValidator,
  genderValidator,
  nameValidator,
  nationalityValidator,
  parseDate
} from '../../validators/passenger.validators';

@Component({
  selector: 'app-passenger-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './passenger-form.component.html'
})
export class PassengerFormComponent implements OnChanges {
  /** The passenger currently being edited, or null when nothing is selected. */
  @Input() passenger: Passenger | null = null;

  /** Emitted when the user saves a valid form. */
  @Output() save = new EventEmitter<Passenger>();

  form: FormGroup;
  submitted = false;
  /** Stays true while no row has been selected yet, to keep the Save button disabled. */
  locked = true;

  genderOptions = ['', 'Male', 'Female', 'Unknown'];

  constructor(private fb: FormBuilder) {
    this.form = this.fb.group({
      firstName: ['', [Validators.required, nameValidator()]],
      lastName: ['', [Validators.required, nameValidator()]],
      gender: ['', [Validators.required, genderValidator()]],
      dateOfBirth: ['', [Validators.required, dateOfBirthValidator()]],
      nationality: ['', [Validators.required, nationalityValidator()]]
    });
    this.form.disable();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['passenger']) {
      if (this.passenger) {
        this.locked = false;
        this.form.enable();
        this.form.reset({
          firstName: this.passenger.firstName,
          lastName: this.passenger.lastName,
          gender: this.passenger.gender,
          // convert dd-MM-yyyy -> dd/MM/yyyy for the text input
          dateOfBirth: this.convertToIsoDate(this.passenger.dateOfBirth),
          nationality: this.passenger.nationality
        });
        this.submitted = false;
      }
    }
  }

  onSave(): void {
    this.submitted = true;
    this.form.markAllAsTouched();
    if (this.form.invalid) return;

    const v = this.form.value;
    const dob = parseDate(v.dateOfBirth);
    const dobStr = dob
      ? `${this.pad(dob.getDate())}-${this.pad(dob.getMonth() + 1)}-${dob.getFullYear()}`
      : v.dateOfBirth;

    const updated: Passenger = {
      firstName: v.firstName,
      lastName: v.lastName,
      gender: v.gender,
      dateOfBirth: dobStr,
      nationality: v.nationality
    };
    this.save.emit(updated);
  }

  onClear(): void {
    this.form.reset({
      firstName: '',
      lastName: '',
      gender: '',
      dateOfBirth: '',
      nationality: ''
    });
    this.form.disable();
    this.locked = true;
    this.submitted = false;
  }

  // --- Error-visibility helpers ---
  showErr(ctrl: string, kind: 'invalid' | 'required'): boolean {
    const c = this.form.get(ctrl);
    if (!c || !this.submitted) return false;
    if (kind === 'required') return c.hasError('required');
    return c.invalid && !c.hasError('required');
  }

  errFor(ctrl: string, label: string): string {
    const c = this.form.get(ctrl);
    if (!c || !this.submitted || c.valid) return '';
    if (c.hasError('required')) return `Invalid ${label}`;
    return `Invalid ${label}`;
  }

  private pad(n: number): string {
    return n < 10 ? `0${n}` : String(n);
  }
  /** Converts dd-MM-yyyy (display format) to yyyy-MM-dd (HTML date input format). */
  private convertToIsoDate(display: string): string {
    if (!display) return '';
    const match = /^(\d{2})-(\d{2})-(\d{4})$/.exec(display);
    return match ? `${match[3]}-${match[2]}-${match[1]}` : '';
  }
}