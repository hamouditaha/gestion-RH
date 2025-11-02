import { Component, OnInit, ViewChild } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatDialog } from '@angular/material/dialog';
import { HttpClient } from '@angular/common/http';

interface BulletinSalaire {
  id?: number;
  employeeId: number;
  employeeName: string;
  mois: string;
  annee: number;
  salaireBase: number;
  heuresTravaillees: number;
  heuresSupplementaires: number;
  deductions: number;
  salaireNet: number;
  dateGeneration: string;
}

@Component({
  selector: 'app-salaries',
  templateUrl: './salaries.component.html',
  styleUrls: ['./salaries.component.css']
})
export class SalariesComponent implements OnInit {
  displayedColumns: string[] = ['employeeName', 'mois', 'annee', 'salaireBase', 'salaireNet', 'dateGeneration', 'actions'];
  dataSource = new MatTableDataSource<BulletinSalaire>();
  bulletins: BulletinSalaire[] = [];
  isLoading = false;

  // Statistics
  stats = {
    totalBulletins: 0,
    totalSalaries: 0,
    averageSalary: 0,
    currentMonthBulletins: 0
  };

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  private apiUrl = 'http://localhost:8080/api/salaries';

  constructor(private http: HttpClient, private dialog: MatDialog) { }

  ngOnInit(): void {
    this.loadBulletins();
    this.loadStats();
  }

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }

  loadBulletins(): void {
    this.isLoading = true;
    this.http.get<BulletinSalaire[]>(this.apiUrl).subscribe({
      next: (data) => {
        this.bulletins = data;
        this.dataSource.data = data;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Erreur lors du chargement des bulletins:', error);
        // Mock data for demonstration
        this.bulletins = [
          {
            id: 1,
            employeeId: 1,
            employeeName: 'Jean Dupont',
            mois: 'Janvier',
            annee: 2024,
            salaireBase: 2500,
            heuresTravaillees: 160,
            heuresSupplementaires: 8,
            deductions: 150,
            salaireNet: 2420,
            dateGeneration: '2024-01-31'
          },
          {
            id: 2,
            employeeId: 2,
            employeeName: 'Marie Martin',
            mois: 'Janvier',
            annee: 2024,
            salaireBase: 2300,
            heuresTravaillees: 152,
            heuresSupplementaires: 0,
            deductions: 200,
            salaireNet: 2180,
            dateGeneration: '2024-01-31'
          }
        ];
        this.dataSource.data = this.bulletins;
        this.isLoading = false;
      }
    });
  }

  loadStats(): void {
    this.http.get<any>(`${this.apiUrl}/stats`).subscribe({
      next: (data) => {
        this.stats = data;
      },
      error: (error) => {
        console.error('Erreur lors du chargement des statistiques:', error);
        // Mock stats
        this.stats = {
          totalBulletins: 45,
          totalSalaries: 112500,
          averageSalary: 2500,
          currentMonthBulletins: 12
        };
      }
    });
  }

  applyFilter(event: Event): void {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();

    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  generateBulletin(employeeId: number): void {
    this.http.post(`${this.apiUrl}/generate/${employeeId}`, {}).subscribe({
      next: () => {
        this.loadBulletins();
        this.loadStats();
      },
      error: (error) => {
        console.error('Erreur lors de la génération du bulletin:', error);
      }
    });
  }

  viewBulletin(bulletin: BulletinSalaire): void {
    // Open bulletin in new window or download PDF
    window.open(`${this.apiUrl}/${bulletin.id}/pdf`, '_blank');
  }

  sendBulletinByEmail(bulletin: BulletinSalaire): void {
    this.http.post(`${this.apiUrl}/${bulletin.id}/send-email`, {}).subscribe({
      next: () => {
        // Show success message
        console.log('Bulletin envoyé par email');
      },
      error: (error) => {
        console.error('Erreur lors de l\'envoi du bulletin:', error);
      }
    });
  }

  calculateSalaries(): void {
    this.http.post(`${this.apiUrl}/calculate-all`, {}).subscribe({
      next: () => {
        this.loadBulletins();
        this.loadStats();
      },
      error: (error) => {
        console.error('Erreur lors du calcul des salaires:', error);
      }
    });
  }

  refreshData(): void {
    this.loadBulletins();
    this.loadStats();
  }

  getMonthName(mois: string): string {
    const months: { [key: string]: string } = {
      '01': 'Janvier', '02': 'Février', '03': 'Mars', '04': 'Avril',
      '05': 'Mai', '06': 'Juin', '07': 'Juillet', '08': 'Août',
      '09': 'Septembre', '10': 'Octobre', '11': 'Novembre', '12': 'Décembre'
    };
    return months[mois] || mois;
  }
}
