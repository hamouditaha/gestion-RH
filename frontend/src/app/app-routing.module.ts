import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  {
    path: 'dashboard',
    loadChildren: () => import('./modules/dashboard/dashboard.module').then(m => m.DashboardModule)
  },
  {
    path: 'employees',
    loadChildren: () => import('./modules/employees/employees.module').then(m => m.EmployeesModule)
  },
  {
    path: 'presences',
    loadChildren: () => import('./modules/presences/presences.module').then(m => m.PresencesModule)
  },
  {
    path: 'salaries',
    loadChildren: () => import('./modules/salaries/salaries.module').then(m => m.SalariesModule)
  },
  {
    path: 'qr-scanner',
    loadChildren: () => import('./modules/qr-scanner/qr-scanner.module').then(m => m.QrScannerModule)
  },
  { path: '**', redirectTo: '/dashboard' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
