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

  constructor(private service: RequestService, private router: Router) { }

  ngOnInit(): void {
    this.getAllItem();
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

  redirect(id: number) {
    this.router.navigateByUrl("/all/"+id);
  }

}
