export interface Passenger {
  firstName: string;
  lastName: string;
  gender: string;
  dateOfBirth: string; // dd-MM-yyyy
  nationality: string;
}

export interface RowError {
  row: number;
  invalidColumns: string[];
}

export interface UploadResponse {
  success: boolean;
  flightNo: string;
  fileName: string;
  passengers: Passenger[];
  errors: RowError[];
}
