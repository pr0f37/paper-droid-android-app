package paperdroid.application;

import instapaper.api.ApplicationDatabaseHelper;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.ListFragment;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class FavouriteListFragment extends ListFragment{
	private ApplicationDatabaseHelper _dbh = null;
	private int[] _articleIds; 
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
	 	showDetails(position);
	}

	 
	
	 void showDetails(int index) {
	    
	        Intent intent = new Intent();
	        intent.setClass(getActivity(), DetailsActivity.class);
	        intent.putExtra("id", _articleIds[index]);
	        intent.putExtra("folderId", 0);
	        startActivity(intent);
	
	
	        
	    }
	
	
	@Override
	public void onResume(){
		super.onResume();
		populateList();
	}
	
	
	public ArrayList<HashMap<String,String>> fetchArchivedBookmarks(){
		SQLiteDatabase db =  _dbh.getReadableDatabase();
		ArrayList<HashMap<String,String>> bookmarks = new ArrayList<HashMap<String,String>>();
		HashMap<String,String> item;
		
		String[] col = {"Id", "Title", "Description"};
		Cursor c = db.query("Bookmarks", col, "Starred = 1 OR FolderId = -2", null, null, null, null);
		
		if(c.getCount() != 0){
			_articleIds = new int[c.getCount()];
			int i = 0;
			while(c.moveToNext()){
				 item = new HashMap<String,String>();
			      item.put( "title",c.getString(c.getColumnIndex("Title")));
			      item.put( "description", c.getString(c.getColumnIndex("Description")));
				bookmarks.add(item);
				_articleIds[i++] = c.getInt(c.getColumnIndex("Id"));
			}
		}
		db.close();
		return bookmarks;
	}
	
	public void populateList(){
		_dbh = new ApplicationDatabaseHelper(getActivity().getApplicationContext());
		ArrayList<HashMap<String,String>> list =  fetchArchivedBookmarks();
		setListAdapter(new SimpleAdapter(getActivity().getApplicationContext(),
										list,
										R.layout.list_item ,
										new String[] { "title","description" },
										new int[] {R.id.bookmark_header, R.id.bookmark_desc}));
		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
	}


}
