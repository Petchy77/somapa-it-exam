import { Injectable } from '@angular/core';
import { Passenger } from '../models/passenger.model';

/**
 * Holds the successfully-uploaded payload so the Result page can render it
 * after a navigate().  Cleared when the user cancels.
 */
@Injectable({ providedIn: 'root' })
export class ResultStore {
  flightNo = '';
  fileName = '';
  file: File | null = null;
  passengers: Passenger[] = [];

  set(flightNo: string, file: File, passengers: Passenger[]): void {
    this.flightNo = flightNo;
    this.file = file;
    this.fileName = file.name;
    this.passengers = passengers.map((p) => ({ ...p }));
  }

  clear(): void {
    this.flightNo = '';
    this.fileName = '';
    this.file = null;
    this.passengers = [];
  }

  hasData(): boolean {
    return this.passengers.length > 0;
  }
}
