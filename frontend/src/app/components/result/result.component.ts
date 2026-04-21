import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

import { PassengerFormComponent } from '../passenger-form/passenger-form.component';
import { PassengerTableComponent } from '../passenger-table/passenger-table.component';
import { ResultStore } from '../../services/result.store';
import { Passenger } from '../../models/passenger.model';

@Component({
  selector: 'app-result',
  standalone: true,
  imports: [CommonModule, PassengerFormComponent, PassengerTableComponent],
  templateUrl: './result.component.html'
})
export class ResultComponent implements OnInit {
  @ViewChild(PassengerFormComponent) formComp?: PassengerFormComponent;
  @ViewChild(PassengerTableComponent) tableComp?: PassengerTableComponent;

  passengers: Passenger[] = [];
  fileName = '';
  flightNo = '';
  originalFile: File | null = null;

  /** The passenger currently being edited (a clone, not the row itself). */
  editing: Passenger | null = null;
  /** Index in `passengers` being edited, or -1 when nothing is selected. */
  editingIndex = -1;

  constructor(private store: ResultStore, private router: Router) {}

  ngOnInit(): void {
    if (!this.store.hasData()) {
      this.router.navigate(['/']);
      return;
    }
    this.passengers = this.store.passengers.map((p) => ({ ...p }));
    this.fileName = this.store.fileName;
    this.flightNo = this.store.flightNo;
    this.originalFile = this.store.file;
  }

  onEditRow(evt: { index: number; passenger: Passenger }): void {
    this.editingIndex = evt.index;
    // Pass a clone so edits don't mutate the row until the user confirms Save.
    this.editing = { ...evt.passenger };
  }

  onFormSave(updated: Passenger): void {
    if (this.editingIndex >= 0) {
      this.passengers[this.editingIndex] = updated;
    }
    this.editing = null;
    this.editingIndex = -1;
    // Reset the form back to disabled state.
    this.formComp?.onClear();
  }

  /** Delegates to the table's export (POI-generated) download. */
  onSaveExcel(): void {
    this.tableComp?.onSaveExcel();
  }

  onCancel(): void {
    this.store.clear();
    this.router.navigate(['/']);
  }
}
