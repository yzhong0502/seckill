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
import { Router } from '@angular/router';
import { RequestService } from '../service/request.service';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent implements OnInit {
  form: FormGroup = this.fb.group({
    name:[null, [Validators.required, Validators.pattern("[0-9a-zA-Z]*")]],
    telphone:[null, [Validators.required, Validators.pattern("[0-9]*")]],
    gender:[null],
    age:[null, Validators.min(0)],
    address:[null, Validators.required],
    password:[null, Validators.required]
  });

  verified: boolean = false;
  getOtp: boolean = false;

  constructor(private fb : FormBuilder, private service: RequestService, private router:Router) {
  }

  ngOnInit(): void {
  }

  register(): void {
    console.log(this.form.value);
    this.service.register(this.form.value).subscribe(response=>{
      if (response.status==='success') {
        this.router.navigateByUrl('/seckill');
      } else {
        alert("No record found. Please register!");
        this.form.reset();
      }
    }); 
  }

  getOTP() {   
    this.service.getOTP({
      telphone: this.form.controls['telphone'].value
    }).subscribe(response=>{
      if (response.status==='success') {
        this.getOtp = true;
        alert("OTP sent. Please check your cellphone.");
        this.verified = true;
      } else {
        alert("Unknown error!");
      }
    })
  }
}