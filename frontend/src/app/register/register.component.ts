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
  veriform: FormGroup = this.fb.group({
    otp:[null, [Validators.required, Validators.pattern("[0-9]*")]]
  });
  teleform: FormGroup = this.fb.group({
    telphone:[null, [Validators.required, Validators.pattern("[0-9]*")]]
  });
  form: FormGroup = this.fb.group({
    name:[null, [Validators.required, Validators.pattern("[0-9a-zA-Z]*")]],
    gender:[null],
    age:[null, Validators.min(0)],
    address:[null, Validators.required],
    password:[null, Validators.required]
  });

  verified: boolean = false;
  sentOtp: boolean = false;
  telephone: string = "";

  constructor(private fb : FormBuilder, private service: RequestService, private router:Router) {
  }

  ngOnInit(): void {
  }

  register(): void {
    console.log(this.form.value);
    this.service.register({
      name: this.form.controls['name'].value,
      gender: this.form.controls['gender'].value,
      age: this.form.controls['age'].value,
      address: this.form.controls['address'].value,
      encryptedPassword: this.form.controls['password'].value,
      registerMode: "telephone",
      telphone:this.telephone
    }).subscribe(response=>{
      console.log(response);
      if (response.status==='success') {
        window.localStorage.setItem("userId", response.data.id);
        alert("Successfully registered!");
        this.router.navigateByUrl('/all');
      } else {
        alert("Register failed! "+ response.data.errMsg);
      }
    }); 
  }

  getOTP() {   
    console.log(this.teleform.controls['telphone'].value);
    this.service.getOTP({
      telphone: this.teleform.controls['telphone'].value
    }).subscribe(response=>{
      console.log(response);
      if (response.status==='success') {
        this.sentOtp = true;
        this.telephone = this.teleform.controls['telphone'].value;
        alert("OTP sent. Please check your cellphone.");
      } else {
        alert("Unknown error! "+ response.data.errMsg);
      }
    })
  }

  verify(otp: string) {
    console.log(otp);
    this.service.verify({
      telphone: this.telephone,
      otp: otp
    }).subscribe(response=>{
      console.log(response);
      if (response.status==='success') {
        this.verified = true;
        alert("OTP verified, please fill info to register!");
      } else {
        alert("Unknown error! "+ response.data.errMsg);
      }
    })
  }
}