import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from 'src/environments/environment';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class RequestService {
  private loginUrl: string = "/user/login";
  private registerUrl: string = "/user/register";
  private otpUrl: string = "/user/otp";
  private verifyUrl: string = "/user/verify";


  constructor(private http: HttpClient) { }

  login(data: any): Observable<any>{
    return this.http.post(environment.REQUEST_HOME + this.loginUrl, data);
  }

  register(data: any): Observable<any>{
    return this.http.post(environment.REQUEST_HOME + this.registerUrl, data);
  }

  getOTP(data: any): Observable<any>{
    console.log(data);
    return this.http.post(environment.REQUEST_HOME + this.otpUrl, data);
  }

  verify(data: any): Observable<any>{
    console.log(data);
    return this.http.get(environment.REQUEST_HOME + this.verifyUrl + "?phone="+data.telphone+"&otp="+data.otp);
  }
}
