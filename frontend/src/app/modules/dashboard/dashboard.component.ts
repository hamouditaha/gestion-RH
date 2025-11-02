import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {

  stats = {
    totalEmployees: 0,
    presentToday: 0,
    absentToday: 0,
    totalPresences: 0
  };

  constructor(private router: Router) { }

  ngOnInit(): void {
    // TODO: Load dashboard statistics from API
    this.loadDashboardStats();
  }

  loadDashboardStats(): void {
    // Mock data for now - will be replaced with API calls
    this.stats = {
      totalEmployees: 25,
      presentToday: 18,
      absentToday: 7,
      totalPresences: 1247
    };
  }

  navigateToModule(module: string): void {
    this.router.navigate([`/${module}`]);
  }
}
