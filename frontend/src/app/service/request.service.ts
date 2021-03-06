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
  private allItemUrl: string = "/item/all";
  private itemUrl: string = "/item/get/";

  private buyItemUrl: string = "/order/buy";
  private seckillTokenUrl: string = "/order/seckillToken";
  private cancelOrderUrl: string = "/order/cancel/{id}";
  public verifyCodeUrl: string = environment.REQUEST_HOME+"/order/verifyCode?token=";


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

  getAll(): Observable<any> {
    return this.http.get(environment.REQUEST_HOME + this.allItemUrl);
  }

  getItem(id :number): Observable<any> {
    return this.http.get(environment.REQUEST_HOME + this.itemUrl + id);

  }

  getPromoToken(itemId: number, token:string, promoId: number, verifyCode: string): Observable<any> {
    let url = environment.REQUEST_HOME + this.seckillTokenUrl + "?itemId="+itemId+"&token="+token+"&promoId=" + promoId+"&code="+verifyCode;
    return this.http.get(url);
  }

  buyItem(itemId: number, amount: number, token: string, promoId: number | null, promoToken: string | null): Observable<any> {
    let url = environment.REQUEST_HOME + this.buyItemUrl + "?itemId="+itemId+"&amount="+amount+"&token="+token;
    if (promoId != null) {
      url = url + "&promoId=" + promoId;
    }
    if (promoToken != null) {
      url = url + "&promoToken=" + promoToken;
    }
    return this.http.get(url);
  }
}
