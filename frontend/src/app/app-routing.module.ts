import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { TabComponent } from './tab/tab.component';
import { LoginComponent } from './login/login.component';
import { SeckillComponent } from './seckill/seckill.component';
import { AddItemComponent } from './add-item/add-item.component';
import { ShoppingListComponent } from './shopping-list/shopping-list.component';
import { CanBuy } from './helper/router-guard';
import { ItemDetailComponent } from './item-detail/item-detail.component';

const routes: Routes = [
  {path:"", component:TabComponent},
  {path:"login", component:LoginComponent},
  {path:"seckill", component: SeckillComponent},
  {path:"addItem", component: AddItemComponent},
  {path:"all", component: ShoppingListComponent, canActivate: [CanBuy]},
  {path:"all/:id", component: ItemDetailComponent, canActivate: [CanBuy]},
  {path:"**", redirectTo:""}

  
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
