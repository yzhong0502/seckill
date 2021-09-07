import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { environment } from 'src/environments/environment';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class RequestService {
  private loginUrl: string = "/user/login";
  private registerUrl: string = "/user/register";
  private otpUrl: string = "/user/otp";
  private verifyUrl: string = "/user/verify";

  private addItemUrl: string = "/item/create";


  constructor(private http: HttpClient) { }

  login(data: any): Observable<any>{
    console.log(data);
    return this.http.post(environment.REQUEST_HOME + this.loginUrl, data);
  }

  register(data: any): Observable<any>{
    console.log(data);
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

  addItem(data: any): Observable<any> {
    console.log(data);
    return this.http.post(environment.REQUEST_HOME + this.addItemUrl + "?price="+ data.price, data);
  }

  errorHandler(error : HttpErrorResponse) {
    return throwError(error.message);
  }
}
