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
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  form: FormGroup = this.fb.group({
    telphone:[null, [Validators.required, Validators.pattern("[0-9]*")]],
    password:[null, Validators.required]
  });

  constructor(private fb : FormBuilder, private service: RequestService, private router:Router) {
  }

  ngOnInit(): void {
  }

  login(): void {
    console.log(this.form.value);
    this.service.login(this.form.value).subscribe(response=>{
      console.log(response);
      if (response.status==='success') {
        window.localStorage.setItem("token", response.data);
        alert("Successfully logged in!");
        this.router.navigateByUrl('/all');
      } else {
        alert("Login failed! "+ response.data.errMsg);
        this.form.reset();
      }
    });
  
  }

  

}
