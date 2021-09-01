import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SeckillComponent } from './seckill.component';

describe('SeckillComponent', () => {
  let component: SeckillComponent;
  let fixture: ComponentFixture<SeckillComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SeckillComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SeckillComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
