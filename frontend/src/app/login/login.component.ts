import { Component, OnInit } from '@angular/core';
import {
  AbstractControl,
  ControlValueAccessor,
  FormBuilder,
  FormControl,
  FormGroup,
  NgControl,
  Validators
} from '@angular/forms';
import {MAT_FORM_FIELD, MatFormField, MatFormFieldControl} from '@angular/material/form-field';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  form: FormGroup = this.fb.group({
    name:[null, [Validators.required, Validators.pattern("[0-9a-zA-Z]")]],
    phone:[null, [Validators.required, Validators.maxLength(11), Validators.minLength(11), Validators.pattern("[0-9]")]],
    gender:[null],
    age:[null, Validators.min(0)],
    address:[null, Validators.required]
  });

  constructor(private fb : FormBuilder) {
  }

  ngOnInit(): void {
  }

  onSubmit(): void {
    console.log(this.form);
  }

  

}
