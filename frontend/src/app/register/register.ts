import { Component, OnInit } from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {Auth} from "../services/auth";
import {NgForOf, NgIf} from "@angular/common";
import {Router, RouterLink} from "@angular/router";


@Component({
  selector: 'app-register',
  imports: [
    ReactiveFormsModule,
    NgForOf,
    NgIf,
    RouterLink,
  ],
  templateUrl: './register.html',
  styleUrl: './register.css'
})
export class Register implements OnInit {
  registerForm!: FormGroup;
  roles: string[] = [];
  hospitals: any[] = [];
  departments: any[] = [];
  constructor( private router: Router ,private fb: FormBuilder, private registerService: Auth) {}

  ngOnInit() {
    this.registerService.getRoles().subscribe(data => {
      console.log('ROLES:', data);
      this.roles = data;

    });
    this.registerService.getDepartments().subscribe(data => {
      console.log('departments:', data);
      this.departments = data;

    });
    this.registerForm = this.fb.group({
      username: ['', [Validators.required, Validators.maxLength(50)]],
      password: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(40)]],
      email: [''],
      fullName: ['', [Validators.required, Validators.maxLength(50)]],
      phone: ['', [Validators.required, Validators.maxLength(50)]],
      role: ['', Validators.required],
      hospitalId: [null],
      departmentId: [null]
    });

    // Load roles, hospitals
    this.registerService.getRoles().subscribe(data => this.roles = data);
    this.registerService.getHospitals().subscribe(data => this.hospitals = data);
  }

  isDoctor() {
    return this.registerForm.get('role')?.value === 'DOCTOR';
  }

  onRoleChange() {
    if (!this.isDoctor()) {
      this.registerForm.patchValue({ hospitalId: null, departmentId: null });
      this.departments = [];
    }
  }

  onHospitalChange() {
    const hospitalId = this.registerForm.get('hospitalId')?.value;
    if (hospitalId) {
      this.registerService.getDepartments().subscribe(data => this.departments = data);
    } else {
      this.departments = [];
      this.registerForm.patchValue({ departmentId: null });
    }
  }
  onSubmit() {
    if (this.registerForm.invalid) return;
    this.registerService.register(this.registerForm.value).subscribe(
        (res: any) => {
          if (res && res.token) {
            localStorage.setItem('jwt_token', res.token);
            alert('Registered and logged in!');
            this.router.navigate(['/dashboard']);
          } else {
            alert('Registered! Please log in.');
          }
        },
        err => alert('Registration failed')
    );
  }
}
