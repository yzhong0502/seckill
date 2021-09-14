import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { RequestService } from '../service/request.service';

@Component({
  selector: 'app-shopping-list',
  templateUrl: './shopping-list.component.html',
  styleUrls: ['./shopping-list.component.css']
})
export class ShoppingListComponent implements OnInit {
  itemList: any[] = [];
  amountList: any[] = [];

  constructor(private service: RequestService, private router: Router) { }

  ngOnInit(): void {
    this.getAllItem();
    for (let i = 0; i < this.itemList.length; ++i) {
      this.amountList[i] = 1;
    }
  }

  getAllItem(): void {
    this.service.getAll().subscribe((response)=>{
      if (response.status === "success") {
        this.itemList = response.data;
      } else {
        alert("Fail to get all items! " + response.data.errMsg);
      }
    })
  } 

  buy(i: number) {
    console.log("buying "+i+" for "+this.amountList[i]);
    let userId = window.localStorage.getItem("userId");
    if (userId == null) {
      alert("Error! Can't find user data!");
      this.router.navigateByUrl("http://localhost:4200/");
      return;
    }
    this.service.buyItem(parseInt(userId),this.itemList[i].id, this.amountList[i]).subscribe((response)=>{
      if (response.status === "success") {
        alert("Ordered successfully!");
        location.reload();
      } else {
        alert("Fail to get all items! " + response.data.errMsg);
      }
    })
  }
}
