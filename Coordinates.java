public class Coordinates {
	int x,y;

	Coordinates(int x, int y){
		this.x=x;
		this.y=y;
	}
	
	int getX(){return x;}
	int getY(){return y;}
	
	void setX(int x){this.x = x;}
	void setY(int y){this.y = y;}
	void set(int x, int y){
		this.x=x;
		this.y=y;
	}
	
	public String toString(){
		return x + "/" + y;
	}
	
	  public boolean equals(Coordinates o) {
	      return (this.x == o.x && this.y == o.y);
	 }
	  
	public int hashCode() { //works up untill a value of extending 100
		return x*100+y;  
	}
}
