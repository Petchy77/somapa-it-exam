import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Passenger } from '../../models/passenger.model';
import { PassengerService } from '../../services/passenger.service';

@Component({
  selector: 'app-passenger-table',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './passenger-table.component.html'
})
export class PassengerTableComponent {
  @Input() passengers: Passenger[] = [];
  @Input() fileName = '';
  @Input() flightNo = '';
  /** The original uploaded file — required so the link matches spec §3. */
  @Input() originalFile: File | null = null;

  @Output() edit = new EventEmitter<{ index: number; passenger: Passenger }>();

  downloadingEdited = false;

  constructor(private api: PassengerService) {}

  onEdit(index: number): void {
    this.edit.emit({ index, passenger: this.passengers[index]! });
  }

  /**Downloads the ORIGINAL uploaded .xlsx unchanged. */
  onDownloadOriginal(): void {
    if (!this.originalFile) return;
    const url = URL.createObjectURL(this.originalFile);
    const a = document.createElement('a');
    a.href = url;
    a.download = this.fileName || this.originalFile.name;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    setTimeout(() => URL.revokeObjectURL(url), 0);
  }

  /**Write Excel File" skill listed in*/
  onSaveExcel(): void {
    if (this.downloadingEdited) return;
    this.downloadingEdited = true;

    this.api.export(this.flightNo, this.passengers).subscribe({
      next: (blob: Blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `Passenger_${this.flightNo}.xlsx`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        setTimeout(() => URL.revokeObjectURL(url), 0);
        this.downloadingEdited = false;
      },
      error: () => {
        alert('Failed to generate Excel file');
        this.downloadingEdited = false;
      }
    });
  }
}
