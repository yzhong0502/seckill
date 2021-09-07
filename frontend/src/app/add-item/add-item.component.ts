import { Component, OnInit } from '@angular/core';
import {
  AbstractControl,
  ControlValueAccessor,
  FormBuilder,
  FormControl,
  FormGroup,
  NgControl,
  Validators
} from '@angular/forms';
import { Router } from '@angular/router';
import { RequestService } from '../service/request.service';

@Component({
  selector: 'app-add-item',
  templateUrl: './add-item.component.html',
  styleUrls: ['./add-item.component.css']
})
export class AddItemComponent implements OnInit {
  form: FormGroup = this.fb.group({
    title:[null, Validators.required],
    price:[null, Validators.min(0)],
    stock:[null, Validators.min(0)],
    description:[null, Validators.required],
    imgUrl:[null, Validators.required]
  });

  constructor(private fb: FormBuilder, private service: RequestService, private router: Router) { }

  ngOnInit(): void {
  }

  add(): void {
    this.service.addItem(this.form.value).subscribe(response=>{
      console.log(response);
      if (response.status==='success') {
        alert("Successfully added item!");
        this.form.reset();
      } else {
        alert("Failed to add item! "+ response.data.errMsg);
      }
    },(error)=> {
      console.log(error);
    });
  }
}
