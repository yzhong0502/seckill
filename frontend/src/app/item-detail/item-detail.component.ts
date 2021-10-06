import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { RequestService } from '../service/request.service';
import { environment } from 'src/environments/environment';

@Component({
  selector: 'app-item-detail',
  templateUrl: './item-detail.component.html',
  styleUrls: ['./item-detail.component.css']
})
export class ItemDetailComponent implements OnInit {
  item: any = {};
  private itemId: number = -1;
  amount: number = 1;
  private startTime = 0;
  leftSec = 0;
  private id: any;

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

  ngOnDestroy() {
    if (this.id) {
      clearInterval(this.id);
    }
  }

  getItem(id: number) {
    this.service.getItem(this.itemId).subscribe((response)=>{
      if (response.status === "success") {
        console.log(response.data);
        this.item = response.data;
        if (this.item.promoStatus === 1) {
          this.startTime = new Date(this.item.startDate).getTime();
          this.countDown();
          this.id = setInterval(()=>{
            this.countDown();
          }, 1000);
        }
      } else {
        alert("Fail to get item because " + response.data.errMsg);
        this.router.navigateByUrl("/all");
      }
    });
  }

  countDown(): void {
    let datetime = new Date().getTime();
    let sec = (this.startTime - datetime) / 1000;
    if (sec <= 0) {
      clearInterval(this.id);
      window.location.reload();
    } else {
      this.leftSec = sec;
    }
  }

  buy() {
    let token = window.localStorage.getItem("token");
    if (token == null) {
      alert("Error! Haven't login!");
      this.router.navigateByUrl(environment.HOME_PAGE);
      return;
    }
    this.service.buyItem(this.itemId, this.amount, token, this.item.promoId).subscribe((response)=>{
      if (response.status === "success") {
        alert("Ordered successfully!");
        location.reload();
      } else {
        alert("Fail to get all items! " + response.data.errMsg);
      }
    })
  }
}
