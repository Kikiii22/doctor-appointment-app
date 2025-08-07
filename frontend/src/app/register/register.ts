import { Component, OnInit } from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {Auth} from "../services/auth";
import {NgForOf} from "@angular/common";


@Component({
  selector: 'app-register',
  imports: [
    ReactiveFormsModule,
    NgForOf,
  ],
  templateUrl: './register.html',
  styleUrl: './register.css'
})
export class Register implements OnInit{
  registerForm!: FormGroup;
  departments: any[] = [];

  constructor(
      private fb: FormBuilder,
      private service: Auth
  ) {}
  ngOnInit(): void {
    this.service.getDepartments(1).subscribe({
      next: (data) => {
        console.log('Departments from API:', data);
        this.departments = data;
      },
      error: (err) => {
        console.error('Error fetching departments:', err);
        alert('Failed to fetch departments!');
      }
    });    }


}
