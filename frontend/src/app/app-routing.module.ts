import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { TabComponent } from './tab/tab.component';
import { LoginComponent } from './login/login.component';
import { SeckillComponent } from './seckill/seckill.component';

const routes: Routes = [
  {path:"", component:TabComponent},
  {path:"login", component:LoginComponent},
  {path:"seckill", component: SeckillComponent}
  
  
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
