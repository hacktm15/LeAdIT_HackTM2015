using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace CloudDemo
{
    public class Receipt
    {
        public string date { get; set; }
        public string total { get; set; }

        public Receipt(string date, string total) {
            this.date = date;
            this.total = total;
        }

   
    }

    
}
