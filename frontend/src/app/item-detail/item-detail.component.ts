import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { RequestService } from '../service/request.service';

@Component({
  selector: 'app-item-detail',
  templateUrl: './item-detail.component.html',
  styleUrls: ['./item-detail.component.css']
})
export class ItemDetailComponent implements OnInit {
  item: any = {};
  private itemId: number = -1;
  amount: number = 1;

  constructor(private service : RequestService, private route: ActivatedRoute, private router:Router) { }

  ngOnInit(): void {
    let id = this.route.snapshot.paramMap.get('id');
    if (id != null) {
      this.itemId = parseInt(id);
      console.log("itemId = " + id);
    }
    if (this.itemId < 0) {
      alert("Invalid itemId!");
    } else {
      this.getItem(this.itemId);
    }
  }

  getItem(id: number) {
    this.service.getItem(this.itemId).subscribe((response)=>{
      if (response.status === "success") {
        console.log(response.data);
        this.item = response.data;
      } else {
        alert("Fail to get item because " + response.data.errMsg);
        this.router.navigateByUrl("/all");
      }
    });
  }

  buy() {
    let userId = window.localStorage.getItem("userId");
    if (userId == null) {
      alert("Error! Can't find user data!");
      this.router.navigateByUrl("http://localhost:4200/");
      return;
    }
    this.service.buyItem(parseInt(userId), this.itemId, this.amount).subscribe((response)=>{
      if (response.status === "success") {
        alert("Ordered successfully!");
        location.reload();
      } else {
        alert("Fail to get all items! " + response.data.errMsg);
      }
    })
  }
}
