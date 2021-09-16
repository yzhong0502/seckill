import { Injectable } from "@angular/core";
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from "@angular/router";

@Injectable()
export class CanBuy implements CanActivate {

    constructor(private router: Router){}

    canActivate(
        route: ActivatedRouteSnapshot,
        state: RouterStateSnapshot): boolean {
            if (window.localStorage.getItem("userId") == null) {
                this.router.navigateByUrl("");
                return false;
            }
            return true;
        }
    
}