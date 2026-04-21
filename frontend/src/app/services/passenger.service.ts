import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Passenger, UploadResponse } from '../models/passenger.model';

@Injectable({ providedIn: 'root' })
export class PassengerService {
  private readonly baseUrl = 'http://localhost:8080/api/passengers';

  constructor(private http: HttpClient) {}

  upload(flightNo: string, file: File): Observable<UploadResponse> {
    const form = new FormData();
    form.append('flightNo', flightNo);
    form.append('file', file);
    return this.http.post<UploadResponse>(`${this.baseUrl}/upload`, form);
  }

  /**
   * Asks the backend to generate a fresh .xlsx from the current passenger
   * list. The response is a binary Blob the caller can save to disk.
   */
  export(flightNo: string, passengers: Passenger[]): Observable<Blob> {
    const params = new HttpParams().set('flightNo', flightNo);
    return this.http.post(`${this.baseUrl}/export`, passengers, {
      params,
      responseType: 'blob'
    });
  }
}
