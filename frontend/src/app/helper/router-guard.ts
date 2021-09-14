import { Injectable } from "@angular/core";
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot } from "@angular/router";

@Injectable()
export class CanBuy implements CanActivate {

    constructor(){}

    canActivate(
        route: ActivatedRouteSnapshot,
        state: RouterStateSnapshot): boolean {
            if (window.localStorage.getItem("userId") == null) {
                return false;
            }
            return true;
        }
    
}